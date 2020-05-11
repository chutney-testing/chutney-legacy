package com.chutneytesting.execution.domain.campaign;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignNotFoundException;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.scenario.FailedExecutionAttempt;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load campaigns with {@link CampaignRepository}
 * Run each scenario with @{@link ScenarioExecutionEngine}
 */
public class CampaignExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Campaign.class);

    private final CampaignRepository campaignRepository;
    private final ScenarioExecutionEngine scenarioExecutionEngine;
    private final ExecutionHistoryRepository executionHistoryRepository;
    private final TestCaseRepository testCaseRepository;

    private Map<Long, CampaignExecutionReport> currentCampaignExecutions = new ConcurrentHashMap<>();
    private Map<Long, Boolean> currentCampaignExecutionsStopRequests = new ConcurrentHashMap<>();

    public CampaignExecutionEngine(CampaignRepository campaignRepository,
                                   ScenarioExecutionEngine scenarioExecutionEngine,
                                   ExecutionHistoryRepository executionHistoryRepository,
                                   TestCaseRepository testCaseRepository) {
        this.campaignRepository = campaignRepository;
        this.scenarioExecutionEngine = scenarioExecutionEngine;
        this.executionHistoryRepository = executionHistoryRepository;
        this.testCaseRepository = testCaseRepository;
    }

    public List<CampaignExecutionReport> executeByName(String campaignName) {
        return executeByName(campaignName, null);
    }

    public List<CampaignExecutionReport> executeByName(String campaignName, String environment) {
        List<Campaign> campaigns = campaignRepository.findByName(campaignName);
        return campaigns.stream()
            .map(campaign -> selectExecutionEnvironment(campaign, Optional.ofNullable(environment)))
            .map(this::executeCampaign)
            .collect(Collectors.toList());
    }

    public CampaignExecutionReport executeById(Long campaignId) {
        return executeById(campaignId, null);
    }

    public CampaignExecutionReport executeById(Long campaignId, String environment) {
        return Optional.ofNullable(campaignRepository.findById(campaignId))
            .map(campaign -> selectExecutionEnvironment(campaign, Optional.ofNullable(environment)))
            .map(this::executeCampaign)
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    public Optional<CampaignExecutionReport> currentExecution(Long campaignId) {
        return currentCampaignExecutions.entrySet().stream().filter(e -> e.getKey().equals(campaignId)).map(Entry::getValue).findFirst();
    }

    public List<CampaignExecutionReport> currentExecutions() {
        return new ArrayList<>(currentCampaignExecutions.values());
    }

    public boolean stopExecution(Long executionId) {
        LOGGER.trace("Stop requested for " + executionId);
        Optional.ofNullable(currentCampaignExecutionsStopRequests.computeIfPresent(executionId, (aLong, aBoolean) -> Boolean.TRUE))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(executionId));
        return true;
    }

    public CampaignExecutionReport executeScenarioInCampaign(List<String> failedIds, Campaign campaign) {
        verifyNotAlreadyRunning(campaign);

        Long executionId = campaignRepository.newCampaignExecution();
        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(executionId, campaign.title, !failedIds.isEmpty(), campaign.executionEnvironment());
        currentCampaignExecutions.put(campaign.id, campaignExecutionReport);
        currentCampaignExecutionsStopRequests.put(executionId, Boolean.FALSE);
        try {
            if (failedIds.isEmpty()) {
                return execute(campaign, campaignExecutionReport, campaign.scenarioIds);
            } else {
                return execute(campaign, campaignExecutionReport, failedIds);
            }
        } catch (Exception e) {
            LOGGER.error("Not managed exception occured",e);
            throw new RuntimeException(e);
        } finally {
            campaignExecutionReport.endCampaignExecution();
            LOGGER.info("Save campaign {} execution {} with status {}", campaign.id, campaignExecutionReport.executionId, campaignExecutionReport.status());
            campaignRepository.saveReport(campaign.id, campaignExecutionReport);
            currentCampaignExecutionsStopRequests.remove(executionId);
            currentCampaignExecutions.remove(campaign.id);
        }
    }

    private CampaignExecutionReport execute(Campaign campaign, CampaignExecutionReport campaignExecutionReport, List<String> scenariosToExecute) {
        LOGGER.trace("Execute campaign {} : {}", campaign.id, campaign.title);
        List<TestCase> testCases = scenariosToExecute.stream()
            .map(testCaseRepository::findById)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        campaignExecutionReport.initExecution(testCases, campaign.executionEnvironment());
        Stream<TestCase> scenarioStream;
        if(campaign.parallelRun) {
            scenarioStream = testCases.parallelStream();
        } else {
            scenarioStream = testCases.stream();
        }

        scenarioStream.forEach(testCase ->  {
            // Is stop requested ?
            if (!currentCampaignExecutionsStopRequests.get(campaignExecutionReport.executionId)) {
                // Override scenario dataset by campaign's one
                Map<String, String> ds = new HashMap<>(testCase.dataSet());
                ds.putAll(campaign.dataSet);
                // Init scenario execution in campaign report
                campaignExecutionReport.startScenarioExecution(testCase, campaign.executionEnvironment());
                // Execute scenario
                ScenarioExecutionReportCampaign scenarioExecutionReport = executeScenario(campaign, testCase.withDataSet(ds));
                // Retry one time if failed
                if (campaign.retryAuto && ServerReportStatus.FAILURE.equals(scenarioExecutionReport.status())) {
                    scenarioExecutionReport = executeScenario(campaign, testCase.withDataSet(ds));
                }
                // Add scenario report to campaign's one
                Optional.ofNullable(scenarioExecutionReport)
                    .ifPresent(campaignExecutionReport::endScenarioExecution);
            }
        });
        return campaignExecutionReport;
    }

    private ScenarioExecutionReportCampaign executeScenario(Campaign campaign, TestCase testCase) {
        Long executionId;
        String scenarioName;
        try {
            LOGGER.trace("Execute scenario {} for campaign {}", testCase.id(), campaign.id);
            ScenarioExecutionReport scenarioExecutionReport = scenarioExecutionEngine.execute(testCase, campaign.executionEnvironment());
            executionId = scenarioExecutionReport.executionId;
            scenarioName = scenarioExecutionReport.scenarioName;
        } catch (FailedExecutionAttempt e) {
            LOGGER.warn("Failed execution attempt for scenario {} for campaign {}", testCase.id(), campaign.id);
            executionId = e.executionId;
            scenarioName = e.title;
        } catch (ScenarioNotFoundException | ScenarioNotParsableException se) {
            LOGGER.error("Scenario error for scenario {} for campaign {}", testCase.id(), campaign.id, se);
            // TODO - Do not hide scenario problem
            return null;
        }
        // TODO - why an extra DB request when we already have the report above ?
        ExecutionHistory.Execution execution = executionHistoryRepository.getExecution(testCase.id(), executionId);
        return new ScenarioExecutionReportCampaign(testCase.id(), scenarioName, execution.summary());
    }

    private CampaignExecutionReport executeCampaign(Campaign campaign) {
        return executeScenarioInCampaign(Collections.emptyList(), campaign);
    }

    private void verifyNotAlreadyRunning(Campaign campaign) {
        Optional<CampaignExecutionReport> currentReport = Optional.ofNullable(currentExecutionReport(campaign.id));
        if (currentReport.isPresent() && !currentReport.get().status().isFinal()) {
            throw new CampaignAlreadyRunningException(currentReport.get());
        }
    }

    private Campaign selectExecutionEnvironment(Campaign campaign, Optional<String> environment) {
        environment.ifPresent(campaign::executionEnvironment);
        return campaign;
    }

    private CampaignExecutionReport currentExecutionReport(Long campaignId) {
        return currentCampaignExecutions.get(campaignId);
    }
}

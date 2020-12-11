package com.chutneytesting.execution.domain.campaign;

import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignNotFoundException;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.jira.JiraXrayPlugin;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.scenario.FailedExecutionAttempt;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.instrument.domain.ChutneyMetrics;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load campaigns with {@link CampaignRepository}
 * Run each scenario with @{@link ScenarioExecutionEngine}
 */
public class CampaignExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Campaign.class);

    private ExecutorService executor;
    private final CampaignRepository campaignRepository;
    private final ScenarioExecutionEngine scenarioExecutionEngine;
    private final ExecutionHistoryRepository executionHistoryRepository;
    private final TestCaseRepository testCaseRepository;
    private final DataSetHistoryRepository dataSetHistoryRepository;
    private final JiraXrayPlugin jiraXrayPlugin;
    private final ChutneyMetrics metrics;

    private Map<Long, CampaignExecutionReport> currentCampaignExecutions = new ConcurrentHashMap<>();
    private Map<Long, Boolean> currentCampaignExecutionsStopRequests = new ConcurrentHashMap<>();

    public CampaignExecutionEngine(CampaignRepository campaignRepository,
                                   ScenarioExecutionEngine scenarioExecutionEngine,
                                   ExecutionHistoryRepository executionHistoryRepository,
                                   TestCaseRepository testCaseRepository,
                                   DataSetHistoryRepository dataSetHistoryRepository,
                                   JiraXrayPlugin jiraXrayPlugin,
                                   ChutneyMetrics metrics,
                                   Integer threadForCampaigns) {
        this.campaignRepository = campaignRepository;
        this.scenarioExecutionEngine = scenarioExecutionEngine;
        this.executionHistoryRepository = executionHistoryRepository;
        this.testCaseRepository = testCaseRepository;
        this.dataSetHistoryRepository = dataSetHistoryRepository;
        this.jiraXrayPlugin = jiraXrayPlugin;
        this.metrics = metrics;
        this.executor = Executors.newFixedThreadPool(threadForCampaigns);
        LOGGER.debug("Pool for campaigns created with size {}", threadForCampaigns);
    }

    public List<CampaignExecutionReport> executeByName(String campaignName, String userId) {
        return executeByName(campaignName, null, userId);
    }

    public List<CampaignExecutionReport> executeByName(String campaignName, String environment, String userId) {
        List<Campaign> campaigns = campaignRepository.findByName(campaignName);
        return campaigns.stream()
            .map(campaign -> selectExecutionEnvironment(campaign, Optional.ofNullable(environment)))
            .map(campaign -> executeCampaign(campaign, userId))
            .collect(Collectors.toList());
    }

    public CampaignExecutionReport executeById(Long campaignId, String userId) {
        return executeById(campaignId, null, userId);
    }

    public CampaignExecutionReport executeById(Long campaignId, String environment, String userId) {
        return Optional.ofNullable(campaignRepository.findById(campaignId))
            .map(campaign -> selectExecutionEnvironment(campaign, Optional.ofNullable(environment)))
            .map(campaign -> executeCampaign(campaign, userId))
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    public Optional<CampaignExecutionReport> currentExecution(Long campaignId) {
        return currentCampaignExecutions.entrySet().stream().filter(e -> e.getKey().equals(campaignId)).map(Entry::getValue).findFirst();
    }

    public List<CampaignExecutionReport> currentExecutions() {
        return new ArrayList<>(currentCampaignExecutions.values());
    }

    public void stopExecution(Long executionId) {
        LOGGER.trace("Stop requested for " + executionId);
        Optional.ofNullable(currentCampaignExecutionsStopRequests.computeIfPresent(executionId, (aLong, aBoolean) -> Boolean.TRUE))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(executionId));
    }

    public CampaignExecutionReport executeScenarioInCampaign(List<String> failedIds, Campaign campaign, String userId) {
        verifyNotAlreadyRunning(campaign);

        Long executionId = campaignRepository.newCampaignExecution();
        Optional<Pair<String, Integer>> executionDataSet = findExecutionDataSet(campaign);
        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(executionId, campaign.title, !failedIds.isEmpty(), campaign.executionEnvironment(), executionDataSet.map(Pair::getLeft).orElse(null), executionDataSet.map(Pair::getRight).orElse(null), userId);
        currentCampaignExecutions.put(campaign.id, campaignExecutionReport);
        currentCampaignExecutionsStopRequests.put(executionId, Boolean.FALSE);
        try {
            if (failedIds.isEmpty()) {
                return execute(campaign, campaignExecutionReport, campaign.scenarioIds);
            } else {
                return execute(campaign, campaignExecutionReport, failedIds);
            }
        } catch (Exception e) {
            LOGGER.error("Not managed exception occured", e);
            throw new RuntimeException(e);
        } finally {
            campaignExecutionReport.endCampaignExecution();
            LOGGER.info("Save campaign {} execution {} with status {}", campaign.id, campaignExecutionReport.executionId, campaignExecutionReport.status());
            campaignRepository.saveReport(campaign.id, campaignExecutionReport);
            metrics.onCampaignExecutionEnded(campaign, campaignExecutionReport);
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

        campaignExecutionReport.initExecution(testCases, campaign.executionEnvironment(), campaignExecutionReport.userId);
        try {
            if (campaign.parallelRun) {
                Collection<Callable<Object>> toExecute = Lists.newArrayList();
                for (TestCase t : testCases) {
                    toExecute.add(Executors.callable(() -> executeScenarioInCampaign(campaign, campaignExecutionReport).accept(t)));
                }
                executor.invokeAll(toExecute);
            } else {
                for (TestCase t : testCases) {
                    executor.invokeAll(singleton(Executors.callable(() -> executeScenarioInCampaign(campaign, campaignExecutionReport).accept(t))));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error ", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error ", e);
        }
        return campaignExecutionReport;
    }

    private Consumer<TestCase> executeScenarioInCampaign(Campaign campaign, CampaignExecutionReport campaignExecutionReport) {
        return testCase -> {
            // Is stop requested ?
            if (!currentCampaignExecutionsStopRequests.get(campaignExecutionReport.executionId)) {
                // Init scenario execution in campaign report
                campaignExecutionReport.startScenarioExecution(testCase, campaign.executionEnvironment(), campaignExecutionReport.userId);
                // Execute scenario
                ScenarioExecutionReportCampaign scenarioExecutionReport = executeScenario(campaign, testCase, campaignExecutionReport.userId);
                // Retry one time if failed
                if (campaign.retryAuto && ServerReportStatus.FAILURE.equals(scenarioExecutionReport.status())) {
                    scenarioExecutionReport = executeScenario(campaign, testCase, campaignExecutionReport.userId);
                }
                // Add scenario report to campaign's one
                Optional.ofNullable(scenarioExecutionReport)
                    .ifPresent(serc -> {
                        campaignExecutionReport.endScenarioExecution(serc);
                        // update xray test
                        ExecutionHistory.Execution execution = executionHistoryRepository.getExecution(serc.scenarioId, serc.execution.executionId());
                        jiraXrayPlugin.updateTestExecution(campaign.id, serc.scenarioId, execution.report());
                    });
            }
        };
    }

    private ScenarioExecutionReportCampaign executeScenario(Campaign campaign, TestCase testCase, String userId) {
        Long executionId;
        String scenarioName;
        try {
            LOGGER.trace("Execute scenario {} for campaign {}", testCase.id(), campaign.id);
            ExecutionRequest executionRequest = buildExecutionRequest(campaign, testCase, userId);
            ScenarioExecutionReport scenarioExecutionReport = scenarioExecutionEngine.execute(executionRequest);
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

    private ExecutionRequest buildExecutionRequest(Campaign campaign, TestCase testCase, String userId) {
        String campaignDatasetId = campaign.datasetId;
        // Override scenario dataset by campaign's one
        if (isNotBlank(campaignDatasetId) && testCase instanceof ExecutableComposedTestCase) {
            testCase = ((ExecutableComposedTestCase) testCase).withDataSetId(campaignDatasetId);
            return new ExecutionRequest(testCase, campaign.executionEnvironment(), true, userId);
        } else {
            Map<String, String> ds = new HashMap<>(testCase.parameters());
            ds.putAll(campaign.dataSet);
            return new ExecutionRequest(testCase.withParameters(ds), campaign.executionEnvironment(), userId);
        }
    }

    private Optional<Pair<String, Integer>> findExecutionDataSet(Campaign campaign) {
        String datasetId = campaign.datasetId;
        if (isNotBlank(datasetId)) {
            return of(Pair.of(datasetId, dataSetHistoryRepository.lastVersion(datasetId)));
        }
        return empty();
    }

    private CampaignExecutionReport executeCampaign(Campaign campaign, String userId) {
        return executeScenarioInCampaign(Collections.emptyList(), campaign, userId);
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

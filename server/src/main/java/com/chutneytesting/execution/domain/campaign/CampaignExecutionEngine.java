package com.chutneytesting.execution.domain.campaign;

import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.FailedExecutionAttempt;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngine;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.instrument.ChutneyMetrics;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.tools.Try;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load campaigns with {@link CampaignRepository}
 * Run each scenario with @{@link ScenarioExecutionEngine}
 */
public class CampaignExecutionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(Campaign.class);

    private final ExecutorService executor;
    private final CampaignRepository campaignRepository;
    private final CampaignExecutionRepository campaignExecutionRepository;
    private final ScenarioExecutionEngine scenarioExecutionEngine;
    private final ExecutionHistoryRepository executionHistoryRepository;
    private final TestCaseRepository testCaseRepository;
    private final Optional<DataSetHistoryRepository> dataSetHistoryRepository;
    private final JiraXrayEmbeddedApi jiraXrayEmbeddedApi;
    private final ChutneyMetrics metrics;
    private final DataSetRepository datasetRepository;

    private final Map<Long, Boolean> currentCampaignExecutionsStopRequests = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public CampaignExecutionEngine(CampaignRepository campaignRepository,
                                   CampaignExecutionRepository campaignExecutionRepository,
                                   ScenarioExecutionEngine scenarioExecutionEngine,
                                   ExecutionHistoryRepository executionHistoryRepository,
                                   TestCaseRepository testCaseRepository,
                                   Optional<DataSetHistoryRepository> dataSetHistoryRepository,
                                   JiraXrayEmbeddedApi jiraXrayEmbeddedApi,
                                   ChutneyMetrics metrics,
                                   ExecutorService executorService,
                                   DataSetRepository datasetRepository, ObjectMapper objectMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignExecutionRepository = campaignExecutionRepository;
        this.scenarioExecutionEngine = scenarioExecutionEngine;
        this.executionHistoryRepository = executionHistoryRepository;
        this.testCaseRepository = testCaseRepository;
        this.dataSetHistoryRepository = dataSetHistoryRepository;
        this.jiraXrayEmbeddedApi = jiraXrayEmbeddedApi;
        this.metrics = metrics;
        this.executor = executorService;
        this.datasetRepository = datasetRepository;
        this.objectMapper = objectMapper;
    }

    public CampaignExecutionReport getLastCampaignExecutionReport(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId);
        return campaignExecutionRepository.getLastExecutionReport(campaign.id);
    }

    public List<CampaignExecutionReport> executeByName(String campaignName, String userId) {
        return executeByName(campaignName, null, userId);
    }

    public List<CampaignExecutionReport> executeByName(String campaignName, String environment, String userId) {
        List<Campaign> campaigns = campaignRepository.findByName(campaignName);
        return campaigns.stream()
            .map(campaign -> selectExecutionEnvironment(campaign, environment))
            .map(campaign -> executeCampaign(campaign, userId))
            .collect(Collectors.toList());
    }

    public CampaignExecutionReport executeById(Long campaignId, String userId) {
        return executeById(campaignId, null, userId);
    }

    public CampaignExecutionReport executeById(Long campaignId, String environment, String userId) {
        return ofNullable(campaignRepository.findById(campaignId))
            .map(campaign -> selectExecutionEnvironment(campaign, environment))
            .map(campaign -> executeCampaign(campaign, userId))
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    public Optional<CampaignExecutionReport> currentExecution(Long campaignId) {
        return campaignExecutionRepository.currentExecution(campaignId);
    }

    public List<CampaignExecutionReport> currentExecutions() {
        return campaignExecutionRepository.currentExecutions();
    }

    public void stopExecution(Long executionId) {
        LOGGER.trace("Stop requested for " + executionId);
        ofNullable(currentCampaignExecutionsStopRequests.computeIfPresent(executionId, (aLong, aBoolean) -> Boolean.TRUE))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(executionId, null));
    }

    public CampaignExecutionReport executeScenarioInCampaign(List<String> failedIds, Campaign campaign, String userId) {
        verifyNotAlreadyRunning(campaign);
        Long executionId = campaignRepository.newCampaignExecution(campaign.id);

        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(
            executionId,
            campaign.title,
            !failedIds.isEmpty(),
            campaign.executionEnvironment(),
            isNotBlank(campaign.externalDatasetId) ? campaign.externalDatasetId : null,
            isNotBlank(campaign.externalDatasetId) && dataSetHistoryRepository.isPresent() ? dataSetHistoryRepository.get().lastVersion(campaign.externalDatasetId) : null,
            userId
        );

        campaignExecutionRepository.startExecution(campaign.id, campaignExecutionReport);
        currentCampaignExecutionsStopRequests.put(executionId, Boolean.FALSE);
        try {
            if (failedIds.isEmpty()) {
                return execute(campaign, campaignExecutionReport, campaign.scenarioIds);
            } else {
                return execute(campaign, campaignExecutionReport, failedIds);
            }
        } catch (Exception e) {
            LOGGER.error("Not managed exception occurred", e);
            throw new RuntimeException(e);
        } finally {
            campaignExecutionReport.endCampaignExecution();
            LOGGER.info("Save campaign {} execution {} with status {}", campaign.id, campaignExecutionReport.executionId, campaignExecutionReport.status());
            currentCampaignExecutionsStopRequests.remove(executionId);
            campaignExecutionRepository.stopExecution(campaign.id);

            Try.exec(() -> {
                campaignRepository.saveReport(campaign.id, campaignExecutionReport);
                return null;
            }).ifFailed(e -> LOGGER.error("Error saving report of campaign {} execution {}", campaign.id, campaignExecutionReport.executionId));

            Try.exec(() -> {
                metrics.onCampaignExecutionEnded(campaign, campaignExecutionReport);
                return null;
            }).ifFailed(e -> LOGGER.error("Error saving metrics for campaign {} execution {}", campaign.id, campaignExecutionReport.executionId));
        }
    }

    private CampaignExecutionReport execute(Campaign campaign, CampaignExecutionReport campaignExecutionReport, List<String> scenariosToExecute) {
        LOGGER.trace("Execute campaign {} : {}", campaign.id, campaign.title);
        List<TestCase> testCases = scenariosToExecute.stream()
            .map(testCaseRepository::findExecutableById)
            .filter(Optional::isPresent)
            .map(Optional::get)
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
            ScenarioExecutionReportCampaign scenarioExecutionReport;
            // Is stop requested ?
            if (!currentCampaignExecutionsStopRequests.get(campaignExecutionReport.executionId)) {
                // Init scenario execution in campaign report
                campaignExecutionReport.startScenarioExecution(testCase, campaign.executionEnvironment(), campaignExecutionReport.userId);
                // Execute scenario
                scenarioExecutionReport = executeScenario(campaign, testCase, campaignExecutionReport);
                // Retry one time if failed
                if (campaign.retryAuto && ServerReportStatus.FAILURE.equals(scenarioExecutionReport.status())) {
                    scenarioExecutionReport = executeScenario(campaign, testCase, campaignExecutionReport);
                }
            } else {
                scenarioExecutionReport = generateNotExecutedScenarioExecutionAndReport(campaign, testCase, campaignExecutionReport);
            }
                // Add scenario report to campaign's one
            ofNullable(scenarioExecutionReport)
                .ifPresent(serc -> {
                    campaignExecutionReport.endScenarioExecution(serc);
                    // update xray test
                    ExecutionHistory.Execution execution = executionHistoryRepository.getExecution(serc.scenarioId, serc.execution.executionId());
                    jiraXrayEmbeddedApi.updateTestExecution(campaign.id, campaignExecutionReport.executionId, serc.scenarioId, JiraReportMapper.from(execution.report(), objectMapper));
                });
        };
    }

    private ScenarioExecutionReportCampaign generateNotExecutedScenarioExecutionAndReport(Campaign campaign, TestCase testCase, CampaignExecutionReport campaignExecutionReport) {
        ExecutionRequest executionRequest = buildExecutionRequest(campaign, testCase, campaignExecutionReport);
        ExecutionHistory.Execution execution = scenarioExecutionEngine.saveNotExecutedScenarioExecution(executionRequest);
        return new ScenarioExecutionReportCampaign(testCase.id(), testCase.metadata().title(), execution.summary());
    }


    private ScenarioExecutionReportCampaign executeScenario(Campaign campaign, TestCase testCase, CampaignExecutionReport campaignExecutionReport) {
        Long executionId;
        String scenarioName;
        try {
            LOGGER.trace("Execute scenario {} for campaign {}", testCase.id(), campaign.id);
            ExecutionRequest executionRequest = buildExecutionRequest(campaign, testCase, campaignExecutionReport);
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

    private ExecutionRequest buildExecutionRequest(Campaign campaign, TestCase testCase, CampaignExecutionReport campaignExecutionReport) {
        return executionWithCombinedParametersFromCampaignAndTestCase(campaign, testCase, campaignExecutionReport);
    }

    private ExecutionRequest executionWithCombinedParametersFromCampaignAndTestCase(Campaign campaign, TestCase testCase, CampaignExecutionReport campaignExecutionReport) {
        Map<String, String> executionParameters = new HashMap<>(testCase.executionParameters());
        executionParameters.putAll(campaign.executionParameters);
        DataSet dataset = ofNullable(campaign.externalDatasetId)
            .map(datasetRepository::findById)
            .orElseGet(() -> datasetRepository.findById(testCase.metadata().defaultDataset()));
        return new ExecutionRequest(testCase.usingExecutionParameters(executionParameters), campaign.executionEnvironment(), campaignExecutionReport.userId, dataset, campaignExecutionReport);
    }

    private CampaignExecutionReport executeCampaign(Campaign campaign, String userId) {
        return executeScenarioInCampaign(Collections.emptyList(), campaign, userId);
    }

    private void verifyNotAlreadyRunning(Campaign campaign) {
        Optional<CampaignExecutionReport> currentReport = currentExecution(campaign.id);
        if (currentReport.isPresent() && !currentReport.get().status().isFinal()) {
            throw new CampaignAlreadyRunningException(currentReport.get());
        }
    }

    private Campaign selectExecutionEnvironment(Campaign campaign, String environment) {
        ofNullable(environment).ifPresent(campaign::executionEnvironment);
        return campaign;
    }
}

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
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
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

    public CampaignExecution getLastCampaignExecution(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId);
        return campaignExecutionRepository.getLastExecution(campaign.id);
    }

    public List<CampaignExecution> executeByName(String campaignName, String userId) {
        return executeByName(campaignName, null, userId);
    }

    public List<CampaignExecution> executeByName(String campaignName, String environment, String userId) {
        List<Campaign> campaigns = campaignRepository.findByName(campaignName);
        return campaigns.stream()
            .map(campaign -> selectExecutionEnvironment(campaign, environment))
            .map(campaign -> executeCampaign(campaign, userId))
            .collect(Collectors.toList());
    }

    public CampaignExecution executeById(Long campaignId, String userId) {
        return executeById(campaignId, null, userId);
    }

    public CampaignExecution executeById(Long campaignId, String environment, String userId) {
        return ofNullable(campaignRepository.findById(campaignId))
            .map(campaign -> selectExecutionEnvironment(campaign, environment))
            .map(campaign -> executeCampaign(campaign, userId))
            .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    public Optional<CampaignExecution> currentExecution(Long campaignId) {
        return campaignExecutionRepository.currentExecution(campaignId);
    }

    public List<CampaignExecution> currentExecutions() {
        return campaignExecutionRepository.currentExecutions();
    }

    public void stopExecution(Long executionId) {
        LOGGER.trace("Stop requested for " + executionId);
        ofNullable(currentCampaignExecutionsStopRequests.computeIfPresent(executionId, (aLong, aBoolean) -> Boolean.TRUE))
            .orElseThrow(() -> new CampaignExecutionNotFoundException(executionId, null));
    }

    public CampaignExecution executeScenarioInCampaign(List<String> failedIds, Campaign campaign, String userId) {
        verifyNotAlreadyRunning(campaign);
        Long executionId = campaignRepository.newCampaignExecution(campaign.id);

        CampaignExecution campaignExecution = new CampaignExecution(
            executionId,
            campaign.title,
            !failedIds.isEmpty(),
            campaign.executionEnvironment(),
            isNotBlank(campaign.externalDatasetId) ? campaign.externalDatasetId : null,
            isNotBlank(campaign.externalDatasetId) && dataSetHistoryRepository.isPresent() ? dataSetHistoryRepository.get().lastVersion(campaign.externalDatasetId) : null,
            userId
        );

        campaignExecutionRepository.startExecution(campaign.id, campaignExecution);
        currentCampaignExecutionsStopRequests.put(executionId, Boolean.FALSE);
        try {
            if (failedIds.isEmpty()) {
                return execute(campaign, campaignExecution, campaign.scenarioIds);
            } else {
                return execute(campaign, campaignExecution, failedIds);
            }
        } catch (Exception e) {
            LOGGER.error("Not managed exception occurred", e);
            throw new RuntimeException(e);
        } finally {
            campaignExecution.endCampaignExecution();
            LOGGER.info("Save campaign {} execution {} with status {}", campaign.id, campaignExecution.executionId, campaignExecution.status());
            currentCampaignExecutionsStopRequests.remove(executionId);
            campaignExecutionRepository.stopExecution(campaign.id);

            Try.exec(() -> {
                campaignRepository.saveExecution(campaign.id, campaignExecution);
                return null;
            }).ifFailed(e -> LOGGER.error("Error saving report of campaign {} execution {}", campaign.id, campaignExecution.executionId));

            Try.exec(() -> {
                metrics.onCampaignExecutionEnded(campaign, campaignExecution);
                return null;
            }).ifFailed(e -> LOGGER.error("Error saving metrics for campaign {} execution {}", campaign.id, campaignExecution.executionId));
        }
    }

    private CampaignExecution execute(Campaign campaign, CampaignExecution campaignExecution, List<String> scenariosToExecute) {
        LOGGER.trace("Execute campaign {} : {}", campaign.id, campaign.title);
        List<TestCase> testCases = scenariosToExecute.stream()
            .map(testCaseRepository::findExecutableById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        campaignExecution.initExecution(testCases, campaign.executionEnvironment(), campaignExecution.userId);
        try {
            if (campaign.parallelRun) {
                Collection<Callable<Object>> toExecute = Lists.newArrayList();
                for (TestCase t : testCases) {
                    toExecute.add(Executors.callable(() -> executeScenarioInCampaign(campaign, campaignExecution).accept(t)));
                }
                executor.invokeAll(toExecute);
            } else {
                for (TestCase t : testCases) {
                    executor.invokeAll(singleton(Executors.callable(() -> executeScenarioInCampaign(campaign, campaignExecution).accept(t))));
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error ", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error ", e);
        }
        return campaignExecution;
    }

    private Consumer<TestCase> executeScenarioInCampaign(Campaign campaign, CampaignExecution campaignExecution) {
        return testCase -> {
            ScenarioExecutionCampaign scenarioExecution;
            // Is stop requested ?
            if (!currentCampaignExecutionsStopRequests.get(campaignExecution.executionId)) {
                // Init scenario execution in campaign report
                campaignExecution.startScenarioExecution(testCase, campaign.executionEnvironment(), campaignExecution.userId);
                // Execute scenario
                scenarioExecution = executeScenario(campaign, testCase, campaignExecution);
                // Retry one time if failed
                if (campaign.retryAuto && ServerReportStatus.FAILURE.equals(scenarioExecution.status())) {
                    scenarioExecution = executeScenario(campaign, testCase, campaignExecution);
                }
            } else {
                scenarioExecution = generateNotExecutedScenarioExecutionAndReport(campaign, testCase, campaignExecution);
            }
                // Add scenario report to campaign's one
            ofNullable(scenarioExecution)
                .ifPresent(serc -> {
                    campaignExecution.endScenarioExecution(serc);
                    // update xray test
                    ExecutionHistory.Execution execution = executionHistoryRepository.getExecution(serc.scenarioId, serc.execution.executionId());
                    jiraXrayEmbeddedApi.updateTestExecution(campaign.id, campaignExecution.executionId, serc.scenarioId, JiraReportMapper.from(execution.report(), objectMapper));
                });
        };
    }

    private ScenarioExecutionCampaign generateNotExecutedScenarioExecutionAndReport(Campaign campaign, TestCase testCase, CampaignExecution campaignExecution) {
        ExecutionRequest executionRequest = buildExecutionRequest(campaign, testCase, campaignExecution);
        ExecutionHistory.Execution execution = scenarioExecutionEngine.saveNotExecutedScenarioExecution(executionRequest);
        return new ScenarioExecutionCampaign(testCase.id(), testCase.metadata().title(), execution.summary());
    }


    private ScenarioExecutionCampaign executeScenario(Campaign campaign, TestCase testCase, CampaignExecution campaignExecution) {
        Long executionId;
        String scenarioName;
        try {
            LOGGER.trace("Execute scenario {} for campaign {}", testCase.id(), campaign.id);
            ExecutionRequest executionRequest = buildExecutionRequest(campaign, testCase, campaignExecution);
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
        return new ScenarioExecutionCampaign(testCase.id(), scenarioName, execution.summary());
    }

    private ExecutionRequest buildExecutionRequest(Campaign campaign, TestCase testCase, CampaignExecution campaignExecution) {
        return executionWithCombinedParametersFromCampaignAndTestCase(campaign, testCase, campaignExecution);
    }

    private ExecutionRequest executionWithCombinedParametersFromCampaignAndTestCase(Campaign campaign, TestCase testCase, CampaignExecution campaignExecution) {
        Map<String, String> executionParameters = new HashMap<>(testCase.executionParameters());
        executionParameters.putAll(campaign.executionParameters);
        DataSet dataset = ofNullable(campaign.externalDatasetId)
            .map(datasetRepository::findById)
            .orElseGet(() -> datasetRepository.findById(testCase.metadata().defaultDataset()));
        return new ExecutionRequest(testCase.usingExecutionParameters(executionParameters), campaign.executionEnvironment(), campaignExecution.userId, dataset, campaignExecution);
    }

    private CampaignExecution executeCampaign(Campaign campaign, String userId) {
        return executeScenarioInCampaign(Collections.emptyList(), campaign, userId);
    }

    private void verifyNotAlreadyRunning(Campaign campaign) {
        Optional<CampaignExecution> currentReport = currentExecution(campaign.id);
        if (currentReport.isPresent() && !currentReport.get().status().isFinal()) {
            throw new CampaignAlreadyRunningException(currentReport.get());
        }
    }

    private Campaign selectExecutionEnvironment(Campaign campaign, String environment) {
        ofNullable(environment).ifPresent(campaign::executionEnvironment);
        return campaign;
    }
}

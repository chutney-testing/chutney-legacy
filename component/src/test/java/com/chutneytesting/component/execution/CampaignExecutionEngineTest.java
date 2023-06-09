package com.chutneytesting.component.execution;

import static java.util.Arrays.stream;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.component.dataset.infra.OrientDataSetRepository;
import com.chutneytesting.component.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.server.core.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ScenarioExecutionEngine;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.instrument.ChutneyMetrics;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class CampaignExecutionEngineTest {

    private static ExecutorService executorService;

    private final CampaignRepository campaignRepository = mock(CampaignRepository.class);
    private final CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
    private final ScenarioExecutionEngine scenarioExecutionEngine = mock(ScenarioExecutionEngine.class);
    private final ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private final TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
    private final DataSetHistoryRepository dataSetHistoryRepository = mock(DataSetHistoryRepository.class);
    private final JiraXrayEmbeddedApi jiraXrayPlugin = mock(JiraXrayEmbeddedApi.class);
    private final ChutneyMetrics metrics = mock(ChutneyMetrics.class);
    private final OrientDataSetRepository datasetRepository = mock(OrientDataSetRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    public static void setUpAll() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(2);
        taskExecutor.initialize();
        executorService = new ExecutorServiceAdapter(taskExecutor);
    }

    @Test
    public void should_override_scenario_dataset_with_campaign_dataset_before_execution() {
        // Given
        CampaignExecutionEngine sut = new CampaignExecutionEngine(campaignRepository, campaignExecutionRepository, scenarioExecutionEngine, executionHistoryRepository, testCaseRepository, of(dataSetHistoryRepository), jiraXrayPlugin, metrics, executorService, datasetRepository, objectMapper);
        TestCase composedTestCase = createExecutableComposedTestCase();

        Map<String, String> campaignDataSet = Maps.of(
            "campaign key", "campaign specific value",
            "key", "campaign value"
        );
        Campaign campaign = createCampaign(campaignDataSet, "campaignDataSetId", composedTestCase);

        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);
        when(testCaseRepository.findExecutableById(composedTestCase.id())).thenReturn(of(composedTestCase));
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(any(), any())).thenReturn(executionWithId(42L));

        // When
        sut.executeById(campaign.id, "user");

        // Then
        ArgumentCaptor<ExecutionRequest> argumentCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(scenarioExecutionEngine, times(1)).execute(argumentCaptor.capture());
        List<ExecutionRequest> executionRequests = argumentCaptor.getAllValues();
        assertThat(executionRequests).hasSize(1);
        assertThat(((ExecutableComposedTestCase) executionRequests.get(0).testCase).metadata.defaultDataset())
            .isEqualTo(campaign.externalDatasetId);
    }

    private final static Random campaignIdGenerator = new Random();

    private Long generateId() {
        return (long) campaignIdGenerator.nextInt(1000);
    }

    private ExecutionHistory.Execution executionWithId(Long executionId) {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(executionId)
            .testCaseTitle("...")
            .time(LocalDateTime.now())
            .duration(3L)
            .status(ServerReportStatus.SUCCESS)
            .report("{\"report\":{\"status\":\"SUCCESS\", \"steps\":[]}}")
            .environment("")
            .user("")
            .build();
    }

    private ExecutableComposedTestCase createExecutableComposedTestCase() {
        return new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withDefaultDataset("composableDataSetId")
                .build(),
            ExecutableComposedScenario.builder().build()
        );
    }

    private Campaign createCampaign(Map<String, String> dataSet, String dataSetId, TestCase... testCases) {
        return new Campaign(generateId(), "...", null, stream(testCases).map(TestCase::id).collect(toList()), dataSet, "campaignEnv", false, false, dataSetId, null);
    }
}

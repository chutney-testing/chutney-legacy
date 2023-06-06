package com.chutneytesting.execution.domain.campaign;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Map.entry;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.WaitUtils.awaitDuring;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignNotFoundException;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.dataset.domain.DataSetRepository;
import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.jira.api.ReportForJira;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
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
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StopWatch;

public class CampaignExecutionEngineTest {

    private static ExecutorService executorService;

    private CampaignExecutionEngine sut;

    private final CampaignRepository campaignRepository = mock(CampaignRepository.class);
    private final CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
    private final ScenarioExecutionEngine scenarioExecutionEngine = mock(ScenarioExecutionEngine.class);
    private final ExecutionHistoryRepository executionHistoryRepository = mock(ExecutionHistoryRepository.class);
    private final TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
    private final DataSetHistoryRepository dataSetHistoryRepository = mock(DataSetHistoryRepository.class);
    private final JiraXrayEmbeddedApi jiraXrayPlugin = mock(JiraXrayEmbeddedApi.class);
    private final ChutneyMetrics metrics = mock(ChutneyMetrics.class);
    private final DataSetRepository datasetRepository = mock(DataSetRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();


    private GwtTestCase firstTestCase;
    private GwtTestCase secondTestCase;
    Long firstScenarioExecutionId = 10L;
    Long secondScenarioExecutionId = 20L;

    @BeforeAll
    public static void setUpAll() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(2);
        taskExecutor.initialize();
        executorService = new ExecutorServiceAdapter(taskExecutor);
    }

    @BeforeEach
    public void setUp() {
        sut = new CampaignExecutionEngine(campaignRepository, campaignExecutionRepository, scenarioExecutionEngine, executionHistoryRepository, testCaseRepository, of(dataSetHistoryRepository), jiraXrayPlugin, metrics, executorService, datasetRepository, objectMapper);
        firstTestCase = createGwtTestCase("1");
        secondTestCase = createGwtTestCase("2");
        when(testCaseRepository.findExecutableById(firstTestCase.id())).thenReturn(of(firstTestCase));
        when(testCaseRepository.findExecutableById(secondTestCase.id())).thenReturn(of(secondTestCase));
        when(executionHistoryRepository.getExecution(eq(firstTestCase.id()), or(eq(0L), eq(10L))))
            .thenReturn(executionWithId(firstScenarioExecutionId));
        when(executionHistoryRepository.getExecution(eq(secondTestCase.id()), or(eq(0L), eq(20L))))
            .thenReturn(executionWithId(secondScenarioExecutionId));
    }

    @Test
    public void should_update_jira_xray() {
        // Given
        Campaign campaign = createCampaign(firstTestCase, secondTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));

        // When
        CampaignExecutionReport cer = sut.executeScenarioInCampaign(emptyList(), campaign, "user");

        ArgumentCaptor<ReportForJira> reportForJiraCaptor = ArgumentCaptor.forClass(ReportForJira.class);
        verify(jiraXrayPlugin).updateTestExecution(eq(campaign.id), eq(cer.executionId), eq(firstTestCase.metadata.id), reportForJiraCaptor.capture());

        assertThat(reportForJiraCaptor).isNotNull();

    }

    @Test
    public void should_execute_scenarios_in_sequence_and_store_reports_in_campaign_report_when_executed() {
        // Given
        Campaign campaign = createCampaign(firstTestCase, secondTestCase);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));

        // When
        CampaignExecutionReport campaignExecutionReport = sut.executeScenarioInCampaign(emptyList(), campaign, "user");

        // Then
        verify(testCaseRepository, times(2)).findExecutableById(anyString());
        verify(scenarioExecutionEngine, times(2)).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository, times(4)).getExecution(anyString(), anyLong());

        assertThat(campaignExecutionReport.scenarioExecutionReports()).hasSize(campaign.scenarioIds.size());
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(0).execution.executionId()).isEqualTo(firstScenarioExecutionId);
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(1).execution.executionId()).isEqualTo(secondScenarioExecutionId);
        assertThat(campaignExecutionReport.partialExecution).isFalse();
        verify(campaignRepository).saveReport(campaign.id, campaignExecutionReport);
        verify(metrics).onCampaignExecutionEnded(
            eq(campaign),
            eq(campaignExecutionReport)
        );
    }

    @Test
    public void should_execute_partially_scenarios_requested() {
        // Given
        Campaign campaign = createCampaign(createGwtTestCase("not executed test case"), secondTestCase);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));

        // When
        CampaignExecutionReport campaignExecutionReport = sut.executeScenarioInCampaign(singletonList("2"), campaign, "user");

        // Then
        verify(testCaseRepository, times(1)).findExecutableById(anyString());
        verify(scenarioExecutionEngine, times(1)).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository, times(2)).getExecution(anyString(), anyLong());

        assertThat(campaignExecutionReport.scenarioExecutionReports()).hasSize(1);
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(0).execution.executionId()).isEqualTo(secondScenarioExecutionId);
        assertThat(campaignExecutionReport.partialExecution).isTrue();
        verify(campaignRepository).saveReport(campaign.id, campaignExecutionReport);
    }

    @Test
    public void should_stop_execution_of_scenarios_when_requested() {
        // Given
        Campaign campaign = createCampaign(firstTestCase, secondTestCase);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).then((Answer<ScenarioExecutionReport>) invocationOnMock -> {
            awaitDuring(1, SECONDS);
            return mock(ScenarioExecutionReport.class);
        });

        Long firstScenarioExecutionId = 10L;
        when(executionHistoryRepository.getExecution(eq(firstTestCase.id()), or(eq(0L), eq(10L))))
            .thenReturn(executionWithId(firstScenarioExecutionId));

        // When
        AtomicReference<CampaignExecutionReport> campaignExecutionReport = new AtomicReference<>();

        Executors.newFixedThreadPool(1).submit(() -> campaignExecutionReport.set(sut.executeScenarioInCampaign(emptyList(), campaign, "user")));

        awaitDuring(500, MILLISECONDS);
        sut.stopExecution(0L);
        awaitDuring(1, SECONDS);

        // Then
        verify(scenarioExecutionEngine).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository, times(2)).getExecution(anyString(), anyLong());

        assertThat(campaignExecutionReport.get().status()).isEqualTo(ServerReportStatus.STOPPED);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports()).hasSize(2);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports().get(0).status()).isEqualTo(ServerReportStatus.SUCCESS);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports().get(1).status()).isEqualTo(ServerReportStatus.NOT_EXECUTED);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports()).hasSize(2);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports().get(0).execution.executionId()).isEqualTo(firstScenarioExecutionId);
        assertThat(campaignExecutionReport.get().scenarioExecutionReports().get(1).execution.executionId()).isEqualTo(-1L);
    }

    @Test
    public void should_retry_failed_scenario() {
        // Given
        Campaign campaign = createCampaign(firstTestCase, secondTestCase, true);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(eq(firstTestCase.id()), or(eq(0L), eq(10L)))).thenReturn(failedExecutionWithId(10L));
        when(executionHistoryRepository.getExecution(eq(secondTestCase.id()), or(eq(0L), eq(20L)))).thenReturn(failedExecutionWithId(20L));

        // When
        sut.executeScenarioInCampaign(emptyList(), campaign, "user");

        // Then
        verify(scenarioExecutionEngine, times(4)).execute(any(ExecutionRequest.class));
    }

    @Test
    public void should_execute_scenario_in_parallel() {
        // Given
        Campaign campaign = createCampaign(firstTestCase, secondTestCase, true, false);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).then((Answer<ScenarioExecutionReport>) invocationOnMock -> {
            awaitDuring(1, SECONDS);
            return mock(ScenarioExecutionReport.class);
        });
        when(executionHistoryRepository.getExecution(eq(firstTestCase.id()), or(eq(0L), eq(10L)))).thenReturn(failedExecutionWithId(10L));
        when(executionHistoryRepository.getExecution(eq(secondTestCase.id()), or(eq(0L), eq(20L)))).thenReturn(failedExecutionWithId(20L));

        // When
        StopWatch watch = new StopWatch();
        watch.start();
        sut.executeScenarioInCampaign(emptyList(), campaign, "user");
        watch.stop();

        // Then
        verify(scenarioExecutionEngine, times(2)).execute(any(ExecutionRequest.class));
        assertThat(watch.getTotalTimeSeconds()).isLessThan(1.9);
    }

    @Test
    public void should_throw_when_no_campaign_found_on_execute_by_id() {
        when(campaignRepository.findById(anyLong())).thenReturn(null);
        assertThatThrownBy(() -> sut.executeById(generateId(), ""))
            .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    public void should_throw_when_campaign_already_running() {
        Campaign campaign = createCampaign(1L);

        CampaignExecutionReport mockReport = new CampaignExecutionReport(1L, "", false, "", null, null, "");
        when(campaignExecutionRepository.currentExecution(1L)).thenReturn(Optional.of(mockReport));

        // When
        assertThatThrownBy(() -> sut.executeScenarioInCampaign(null, campaign, "user"))
            .isInstanceOf(CampaignAlreadyRunningException.class);
    }

    @Test
    public void should_generate_campaign_execution_id_when_executed() {
        // Given
        Campaign campaign = createCampaign();

        when(campaignRepository.findByName(campaign.title)).thenReturn(singletonList(campaign));
        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);

        // When
        sut.executeById(campaign.id, "");
        sut.executeByName(campaign.title, "");

        // Then
        verify(campaignRepository).findById(campaign.id);
        verify(campaignRepository).findByName(campaign.title);

        verify(campaignRepository, times(2)).newCampaignExecution(campaign.id);
    }

    @Test
    public void should_execute_campaign_with_given_environment_when_executed_by_id() {
        // Given
        Campaign campaign = createCampaign();
        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);

        // When
        String executionEnv = "executionEnv";
        String executionUser = "executionUser";
        sut.executeById(campaign.id, executionEnv, executionUser);

        // Then
        verify(campaignRepository).findById(campaign.id);
        assertThat(campaign.executionEnvironment()).isEqualTo(executionEnv);
    }

    @Test
    public void should_execute_campaign_with_given_environment_when_executed_by_name() {
        // Given
        Campaign campaign = createCampaign();
        when(campaignRepository.findByName(anyString())).thenReturn(singletonList(campaign));

        // When
        String executionEnv = "executionEnv";
        String executionUser = "executionUser";
        sut.executeByName(campaign.title, executionEnv, executionUser);

        // Then
        verify(campaignRepository).findByName(campaign.title);
        assertThat(campaign.executionEnvironment()).isEqualTo(executionEnv);
    }

    @Test
    public void should_retrieve_current_campaign_executions() {
        CampaignExecutionReport report = new CampaignExecutionReport(1L, 33L, emptyList(), "", false, "", null, null, "");
        CampaignExecutionReport report2 = new CampaignExecutionReport(2L, 42L, emptyList(), "", false, "", null, null, "");
        when(campaignExecutionRepository.currentExecution(1L)).thenReturn(Optional.of(report));
        when(campaignExecutionRepository.currentExecution(2L)).thenReturn(Optional.of(report2));

        Optional<CampaignExecutionReport> campaignExecutionReport = sut.currentExecution(1L);

        assertThat(campaignExecutionReport).isNotEmpty();
        assertThat(campaignExecutionReport.get().campaignId).isEqualTo(33L);
    }

    @Test
    public void should_throw_when_stop_unknown_campaign_execution() {
        assertThatThrownBy(() -> sut.stopExecution(generateId()))
            .isInstanceOf(CampaignExecutionNotFoundException.class);
    }

    @Test
    public void should_throw_when_execute_unknown_campaign_execution() {
        assertThatThrownBy(() -> sut.executeById(generateId(), ""))
            .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    public void should_override_scenario_dataset_with_campaign_dataset_before_execution() {
        // Given
        Map<String, String> gwtTestCaseDataSet = Maps.of(
            "gwt key", "gwt specific value",
            "key", "gwt value"
        );
        TestCase gwtTestCase = createGwtTestCase(gwtTestCaseDataSet);

        Map<String, String> campaignDataSet = Maps.of(
            "campaign key", "campaign specific value",
            "key", "campaign value"
        );
        Campaign campaign = createCampaign(campaignDataSet, "campaignDataSetId", gwtTestCase);

        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);
        when(testCaseRepository.findExecutableById(gwtTestCase.id())).thenReturn(of(gwtTestCase));
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(any(), any())).thenReturn(executionWithId(42L));

        // When
        sut.executeById(campaign.id, "user");

        // Then
        ArgumentCaptor<ExecutionRequest> argumentCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(scenarioExecutionEngine, times(1)).execute(argumentCaptor.capture());
        List<ExecutionRequest> executionRequests = argumentCaptor.getAllValues();
        assertThat(executionRequests).hasSize(1);
        assertThat(((GwtTestCase) executionRequests.get(0).testCase).executionParameters).containsOnly(
            entry("gwt key", gwtTestCaseDataSet.get("gwt key")),
            entry("key", campaignDataSet.get("key")),
            entry("campaign key", "campaign specific value")
        );
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

    private ExecutionHistory.Execution failedExecutionWithId(Long executionId) {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(executionId)
            .testCaseTitle("...")
            .time(LocalDateTime.now())
            .duration(3L)
            .status(ServerReportStatus.FAILURE)
            .report("{\"report\":{\"status\":\"FAILURE\", \"steps\":[]}}")
            .environment("")
            .user("")
            .build();
    }

    private GwtTestCase createGwtTestCase(String id) {
        return GwtTestCase.builder().withMetadata(TestCaseMetadataImpl.builder().withId(id).build()).build();
    }

    private GwtTestCase createGwtTestCase(Map<String, String> dataSet) {
        return GwtTestCase.builder()
            .withMetadata(
                TestCaseMetadataImpl.builder()
                    .withId("gwt")
                    .build()
            )
            .withExecutionParameters(dataSet)
            .build();
    }

    private Campaign createCampaign() {
        return new Campaign(generateId(), "...", null, null, null, "campaignEnv", false, false, null, null);
    }

    private Campaign createCampaign(Long idCampaign) {
        return new Campaign(idCampaign, "campaign1", null, emptyList(), emptyMap(), "env", false, false, null, null);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase) {
        return createCampaign(firstTestCase, secondtTestCase, false, false);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase, boolean retryAuto) {
        return createCampaign(firstTestCase, secondtTestCase, false, retryAuto);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase, boolean parallelRun, boolean retryAuto) {
        return new Campaign(1L, "campaign1", null, Lists.list(firstTestCase.id(), secondtTestCase.id()), emptyMap(), "env", parallelRun, retryAuto, null, null);
    }

    private Campaign createCampaign(Map<String, String> dataSet, String dataSetId, TestCase... testCases) {
        return new Campaign(generateId(), "...", null, stream(testCases).map(TestCase::id).collect(toList()), dataSet, "campaignEnv", false, false, dataSetId, null);
    }
}

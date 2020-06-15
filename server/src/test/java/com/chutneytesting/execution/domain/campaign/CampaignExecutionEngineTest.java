package com.chutneytesting.execution.domain.campaign;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.CampaignNotFoundException;
import com.chutneytesting.design.domain.campaign.CampaignRepository;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.dataset.DataSetHistoryRepository;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.execution.domain.scenario.ScenarioExecutionEngine;
import com.chutneytesting.security.domain.User;
import com.chutneytesting.security.domain.UserService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

@RunWith(MockitoJUnitRunner.class)
public class CampaignExecutionEngineTest {

    private CampaignExecutionEngine sut;

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private ScenarioExecutionEngine scenarioExecutionEngine;
    @Mock
    private ExecutionHistoryRepository executionHistoryRepository;
    @Mock
    private TestCaseRepository testCaseRepository;
    @Mock
    private DataSetHistoryRepository dataSetHistoryRepository;

    @Before
    public void setUp() {
        fakeAuthentication();
        UserService userService = new UserService();
        sut = new CampaignExecutionEngine(campaignRepository, scenarioExecutionEngine, executionHistoryRepository, testCaseRepository, dataSetHistoryRepository, userService);
    }

    @Test
    public void should_execute_scenarios_in_sequence_and_store_reports_in_campaign_report_when_executed() {
        // Given
        TestCase firstTestCase = createGwtTestCase("1");
        GwtTestCase secondtTestCase = createGwtTestCase("2");
        Long firstScenarioExecutionId = 10L;
        Long secondScenarioExecutionId = 20L;
        Campaign campaign = createCampaign(firstTestCase, secondtTestCase);

        when(testCaseRepository.findById(firstTestCase.id())).thenReturn(firstTestCase);
        when(testCaseRepository.findById(secondtTestCase.id())).thenReturn(secondtTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(firstTestCase.id(), 0L))
            .thenReturn(executionWithId(firstScenarioExecutionId));
        when(executionHistoryRepository.getExecution(secondtTestCase.id(), 0L))
            .thenReturn(executionWithId(secondScenarioExecutionId));

        // When
        CampaignExecutionReport campaignExecutionReport = sut.executeScenarioInCampaign(emptyList(), campaign);

        // Then
        verify(testCaseRepository, times(2)).findById(anyString());
        verify(scenarioExecutionEngine, times(2)).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository, times(2)).getExecution(anyString(), anyLong());

        assertThat(campaignExecutionReport.scenarioExecutionReports()).hasSize(campaign.scenarioIds.size());
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(0).execution.executionId()).isEqualTo(firstScenarioExecutionId);
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(1).execution.executionId()).isEqualTo(secondScenarioExecutionId);
        assertThat(campaignExecutionReport.partialExecution).isFalse();
        verify(campaignRepository).saveReport(campaign.id, campaignExecutionReport);
    }


    @Test
    public void should_execute_partially_scenarios_requested() {
        // Given
        TestCase firstTestCase = createGwtTestCase("1");
        GwtTestCase secondtTestCase = createGwtTestCase("2");
        Long secondScenarioExecutionId = 20L;
        Campaign campaign = createCampaign(firstTestCase, secondtTestCase);

        when(testCaseRepository.findById(secondtTestCase.id())).thenReturn(secondtTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(secondtTestCase.id(), 0L))
            .thenReturn(executionWithId(secondScenarioExecutionId));

        // When
        CampaignExecutionReport campaignExecutionReport = sut.executeScenarioInCampaign(singletonList("2"), campaign);

        // Then
        verify(testCaseRepository, times(1)).findById(anyString());
        verify(scenarioExecutionEngine, times(1)).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository, times(1)).getExecution(anyString(), anyLong());

        assertThat(campaignExecutionReport.scenarioExecutionReports()).hasSize(1);
        assertThat(campaignExecutionReport.scenarioExecutionReports().get(0).execution.executionId()).isEqualTo(secondScenarioExecutionId);
        assertThat(campaignExecutionReport.partialExecution).isTrue();
        verify(campaignRepository).saveReport(campaign.id, campaignExecutionReport);
    }

    @Test
    public void should_stop_execution_of_scenarios_when_requested() throws InterruptedException {
        // Given
        TestCase firstTestCase = createGwtTestCase("1");
        GwtTestCase secondTestCase = createGwtTestCase("2");

        Campaign campaign = createCampaign(firstTestCase, secondTestCase);

        when(testCaseRepository.findById(firstTestCase.id())).thenReturn(firstTestCase);
        when(testCaseRepository.findById(secondTestCase.id())).thenReturn(secondTestCase);

        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).then((Answer<ScenarioExecutionReport>) invocationOnMock -> {
            TimeUnit.MILLISECONDS.sleep(1000);
            return mock(ScenarioExecutionReport.class);
        });

        Long firstScenarioExecutionId = 10L;
        when(executionHistoryRepository.getExecution(firstTestCase.id(), 0L))
            .thenReturn(executionWithId(firstScenarioExecutionId));

        // When
        AtomicReference<CampaignExecutionReport> campaignExecutionReport = new AtomicReference<>();

        Executors.newFixedThreadPool(1).submit(() -> {
            fakeAuthentication();
            campaignExecutionReport.set(sut.executeScenarioInCampaign(emptyList(), campaign));
        });

        TimeUnit.MILLISECONDS.sleep(500);
        sut.stopExecution(0L);
        TimeUnit.MILLISECONDS.sleep(1000);

        // Then
        verify(scenarioExecutionEngine).execute(any(ExecutionRequest.class));
        verify(executionHistoryRepository).getExecution(anyString(), anyLong());

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
        TestCase firstTestCase = createGwtTestCase("1");
        TestCase secondtTestCase = createGwtTestCase("2");

        Campaign campaign = createCampaign(firstTestCase, secondtTestCase, true);

        when(testCaseRepository.findById(firstTestCase.id())).thenReturn(firstTestCase);
        when(testCaseRepository.findById(secondtTestCase.id())).thenReturn(secondtTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(firstTestCase.id(), 0L)).thenReturn(failedExecutionWithId(10L));
        when(executionHistoryRepository.getExecution(secondtTestCase.id(), 0L)).thenReturn(failedExecutionWithId(20L));

        // When
        sut.executeScenarioInCampaign(emptyList(), campaign);

        // Then
        verify(scenarioExecutionEngine, times(4)).execute(any(ExecutionRequest.class));
    }

    @Test
    public void should_execute_scenario_in_parallel() {
        // Given
        TestCase firstTestCase = createGwtTestCase("1");
        TestCase secondtTestCase = createGwtTestCase("2");

        Campaign campaign = createCampaign(firstTestCase, secondtTestCase, true, false);

        when(testCaseRepository.findById(firstTestCase.id())).thenReturn(firstTestCase);
        when(testCaseRepository.findById(secondtTestCase.id())).thenReturn(secondtTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).then((Answer<ScenarioExecutionReport>) invocationOnMock -> {
            TimeUnit.SECONDS.sleep(1);
            return mock(ScenarioExecutionReport.class);
        });
        when(executionHistoryRepository.getExecution(firstTestCase.id(), 0L)).thenReturn(failedExecutionWithId(10L));
        when(executionHistoryRepository.getExecution(secondtTestCase.id(), 0L)).thenReturn(failedExecutionWithId(20L));

        // When
        StopWatch watch = new StopWatch();
        watch.start();
        sut.executeScenarioInCampaign(emptyList(), campaign);
        watch.stop();

        // Then
        verify(scenarioExecutionEngine, times(2)).execute(any(ExecutionRequest.class));
        assertThat(watch.getTotalTimeSeconds()).isLessThan(1.9);
    }

    @Test(expected = CampaignNotFoundException.class)
    public void should_throw_when_no_campaign_found_on_execute_by_id() {
        when(campaignRepository.findById(anyLong())).thenReturn(null);
        sut.executeById(generateId());
    }

    @Test(expected = CampaignAlreadyRunningException.class)
    public void should_throw_when_campaign_already_running() {
        Campaign campaign = createCampaign(1L);

        Field currentCampaignExecutionsField = ReflectionUtils.findField(CampaignExecutionEngine.class, "currentCampaignExecutions");
        currentCampaignExecutionsField.setAccessible(true);
        Map<Long, CampaignExecutionReport> field = (Map<Long, CampaignExecutionReport>) ReflectionUtils.getField(currentCampaignExecutionsField, sut);
        CampaignExecutionReport mockReport = new CampaignExecutionReport(1L, "", false, "", null, null, "");
        field.put(1L, mockReport);

        // When
        sut.executeScenarioInCampaign(null, campaign);
    }

    @Test
    public void should_generate_campaign_execution_id_when_executed() {
        // Given
        Campaign campaign = createCampaign();

        when(campaignRepository.findByName(campaign.title)).thenReturn(singletonList(campaign));
        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);

        // When
        sut.executeById(campaign.id);
        sut.executeByName(campaign.title);

        // Then
        verify(campaignRepository).findById(campaign.id);
        verify(campaignRepository).findByName(campaign.title);

        verify(campaignRepository, times(2)).newCampaignExecution();
    }

    @Test
    public void should_execute_campaign_with_given_environment_when_executed_by_id() {
        // Given
        Campaign campaign = createCampaign();
        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);

        // When
        String executionEnv = "executionEnv";
        sut.executeById(campaign.id, executionEnv);

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
        sut.executeByName(campaign.title, executionEnv);

        // Then
        verify(campaignRepository).findByName(campaign.title);
        assertThat(campaign.executionEnvironment()).isEqualTo(executionEnv);
    }

    @Test
    public void should_retrieve_current_campaign_executions() {

        Field currentCampaignExecutionsField = ReflectionUtils.findField(CampaignExecutionEngine.class, "currentCampaignExecutions");
        currentCampaignExecutionsField.setAccessible(true);
        Map<Long, CampaignExecutionReport> field = (Map<Long, CampaignExecutionReport>) ReflectionUtils.getField(currentCampaignExecutionsField, sut);
        CampaignExecutionReport report = new CampaignExecutionReport(1L, 33L, emptyList(), "", false, "", null, null, "");
        CampaignExecutionReport report2 = new CampaignExecutionReport(2L, 42L, emptyList(), "", false, "", null, null, "");
        field.put(1L, report);
        field.put(2L, report2);

        Optional<CampaignExecutionReport> campaignExecutionReport = sut.currentExecution(1L);

        assertThat(campaignExecutionReport).isNotEmpty();
        assertThat(campaignExecutionReport.get().campaignId).isEqualTo(33L);
    }

    @Test(expected = CampaignExecutionNotFoundException.class)
    public void should_throw_when_stop_unknown_campaign_execution() {
        sut.stopExecution(generateId());
    }

    @Test(expected = CampaignNotFoundException.class)
    public void should_throw_when_execute_unknown_campaign_execution() {
        sut.executeById(generateId());
    }

    @Test
    public void should_override_scenario_dataset_with_campaign_dataset_before_execution() {
        // Given
        Map<String, String> gwtTestCaseDataSet = Maps.of(
            "gwt key", "gwt specific value",
            "key", "gwt value"
        );
        TestCase gwtTestCase = createGwtTestCase("gwt", gwtTestCaseDataSet);

        TestCase composableTestCase = createComposableTestCase("composable", "composableDataSetId");

        Map<String, String> campaignDataSet = Maps.of(
            "campaign key", "campaign specific value",
            "key", "campaign value"
        );
        Campaign campaign = createCampaign(campaignDataSet, "campaignDataSetId", gwtTestCase, composableTestCase);

        when(campaignRepository.findById(campaign.id)).thenReturn(campaign);
        when(testCaseRepository.findById(gwtTestCase.id())).thenReturn(gwtTestCase);
        when(testCaseRepository.findById(composableTestCase.id())).thenReturn(composableTestCase);
        when(scenarioExecutionEngine.execute(any(ExecutionRequest.class))).thenReturn(mock(ScenarioExecutionReport.class));
        when(executionHistoryRepository.getExecution(any(), any())).thenReturn(mock(ExecutionHistory.Execution.class));

        // When
        sut.executeById(campaign.id);

        // Then
        ArgumentCaptor<ExecutionRequest> argumentCaptor = ArgumentCaptor.forClass(ExecutionRequest.class);
        verify(scenarioExecutionEngine, times(2)).execute(argumentCaptor.capture());
        List<ExecutionRequest> executionRequests = argumentCaptor.getAllValues();
        assertThat(executionRequests).hasSize(2);
        assertThat(((GwtTestCase) executionRequests.get(0).testCase).dataSet).containsOnly(
            entry("gwt key", gwtTestCaseDataSet.get("gwt key")),
            entry("key", campaignDataSet.get("key")),
            entry("campaign key", "campaign specific value")
        );
        assertThat(((ComposableTestCase) executionRequests.get(1).testCase).metadata.datasetId())
            .hasValue(campaign.datasetId);
    }

    private final static Random campaignIdGenerator = new Random();

    private Long generateId() {
        return (long) campaignIdGenerator.nextInt(1000);
    }

    private void fakeAuthentication(){
        User user = new User();
        user.setId("user_id");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private ExecutionHistory.Execution executionWithId(Long executionId) {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(executionId)
            .testCaseTitle("...")
            .time(LocalDateTime.now())
            .duration(3L)
            .status(ServerReportStatus.SUCCESS)
            .report("")
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
            .report("")
            .environment("")
            .user("")
            .build();
    }

    private GwtTestCase createGwtTestCase(String id) {
        return GwtTestCase.builder().withMetadata(TestCaseMetadataImpl.builder().withId(id).build()).build();
    }

    private GwtTestCase createGwtTestCase(String id, Map<String, String> dataSet) {
        return GwtTestCase.builder()
            .withMetadata(
                TestCaseMetadataImpl.builder()
                    .withId(id)
                    .build()
            )
            .withDataSet(dataSet)
            .build();
    }

    private ComposableTestCase createComposableTestCase(String id, String dataSetId) {
        return new ComposableTestCase(
            id,
            TestCaseMetadataImpl.builder()
                .withDatasetId(dataSetId)
                .build(),
            ComposableScenario.builder().build()
        );
    }

    private Campaign createCampaign() {
        return new Campaign(generateId(), "...", null, null, null, null, "campaignEnv", false, false, null);
    }

    private Campaign createCampaign(Long idCampaign) {
        return new Campaign(idCampaign, "campaign1", null, emptyList(), emptyMap(), null, "env", false, false, null);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase) {
        return createCampaign(firstTestCase, secondtTestCase, false, false);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase, boolean retryAuto) {
        return createCampaign(firstTestCase, secondtTestCase, false, retryAuto);
    }

    private Campaign createCampaign(TestCase firstTestCase, TestCase secondtTestCase, boolean parallelRun, boolean retryAuto) {
        return new Campaign(1L, "campaign1", null, Lists.list(firstTestCase.id(), secondtTestCase.id()), emptyMap(), null, "env", parallelRun, retryAuto, null);
    }

    private Campaign createCampaign(Map<String, String> dataSet, String dataSetId, TestCase... testCases) {
        return new Campaign(generateId(), "...", null, stream(testCases).map(TestCase::id).collect(toList()), dataSet, null, "campaignEnv", false, false, dataSetId);
    }
}

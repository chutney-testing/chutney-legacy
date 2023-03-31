package com.chutneytesting.execution.infra.storage;

import static com.chutneytesting.tools.WaitUtils.awaitDuring;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.campaign.infra.CampaignExecutionReportMapper;
import com.chutneytesting.campaign.infra.CampaignExecutionRepository;
import com.chutneytesting.campaign.infra.CampaignParameterRepository;
import com.chutneytesting.campaign.infra.DatabaseCampaignRepository;
import com.chutneytesting.scenario.domain.TestCaseRepositoryAggregator;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DatabaseExecutionHistoryRepositoryTest extends AbstractLocalDatabaseTest {

    private ExecutionHistoryRepository sut;
    private CampaignRepository campaignRepository;

    @BeforeEach
    public void beforeEach() {
        sut = new DatabaseExecutionHistoryRepository(namedParameterJdbcTemplate);
        initCampaignRepository();
    }

    @Test
    public void repository_is_empty_at_startup() {
        assertThat(sut.getExecutions("1")).isEmpty();
    }

    @Test
    public void execution_summary_is_available_after_storing_sorted_newest_first() {
        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec1", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.FAILURE, "exec3", ""));

        assertThat(sut.getExecutions("1"))
            .extracting(summary -> summary.info().get()).containsExactly("exec3", "exec2", "exec1");
    }

    @Test
    public void last_execution_return_newest_first() {
        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec1", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.FAILURE, "exec3", ""));

        sut.store("2", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec6", ""));
        sut.store("2", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec5", ""));
        sut.store("2", buildDetachedExecution(ServerReportStatus.FAILURE, "exec4", ""));

        Map<String, ExecutionSummary> lastExecutions = sut.getLastExecutions(List.of("1", "2"));
        assertThat(lastExecutions).containsOnlyKeys("1", "2");
        assertThat(lastExecutions.get("1").info().get()).isEqualTo("exec3");
        assertThat(lastExecutions.get("2").info().get()).isEqualTo("exec4");
    }

    @Test
    public void execution_summaries_retrieved_are_limit_to_5() {
        IntStream.range(0, 25).forEach(
            i -> {
                sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec" + i, ""));
                // As order is based on executionTime, if they are stored at the exact same time, check on order may fail
                awaitDuring(20, MILLISECONDS);
            }
        );

        assertThat(sut.getExecutions("1"))
            .extracting(summary -> summary.info().get())
            .containsExactly(
                "exec24",
                "exec23",
                "exec22",
                "exec21",
                "exec20",
                "exec19",
                "exec18",
                "exec17",
                "exec16",
                "exec15",
                "exec14",
                "exec13",
                "exec12",
                "exec11",
                "exec10",
                "exec9",
                "exec8",
                "exec7",
                "exec6",
                "exec5"
            );
    }

    @Test
    public void storage_keeps_all_items() {
        DetachedExecution execution = ImmutableExecutionHistory.DetachedExecution.builder()
            .duration(0L)
            .time(LocalDateTime.now())
            .status(ServerReportStatus.SUCCESS)
            .report("toto")
            .testCaseTitle("Fake title")
            .environment("")
            .user("")
            .build();
        IntStream.range(0, 23).forEach(i -> sut.store("1", execution));

        assertThat(sut.getExecutions("2")).hasSize(0);

        Map<String, Object> queryForMap = namedParameterJdbcTemplate.queryForMap(
            "SELECT count(*) as count FROM SCENARIO_EXECUTION_HISTORY WHERE SCENARIO_ID = 1"
            , new HashMap<>());

        assertThat(queryForMap.get("count"))
            .as("All 23 reports of test scenario")
            .isEqualTo(23L);
    }

    @Test
    public void update_execution_alters_last_one() {
        sut.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec", ""));

        ExecutionSummary last = sut.getExecutions("1").get(0);
        assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
        assertThat(last.info()).contains("exec");

        sut.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

        Execution updatedExecution = sut.getExecution("1", last.executionId());
        assertThat(updatedExecution.status()).isEqualTo(ServerReportStatus.SUCCESS);
        assertThat(updatedExecution.info()).contains("updated");
    }

    @Test
    public void update_preserve_other_executions_order() {
        sut.store("1", buildDetachedExecution(ServerReportStatus.FAILURE, "exec1", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
        sut.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec3", ""));

        ExecutionSummary last = sut.getExecutions("1").get(0);
        assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
        assertThat(last.info()).contains("exec3");

        sut.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

        assertThat(sut.getExecutions("1"))
            .extracting(summary -> summary.info().get())
            .containsExactly(
                "updated",
                "exec2",
                "exec1");
    }

    @Test
    public void update_on_empty_history_throws() {
        assertThatExceptionOfType(ReportNotFoundException.class)
            .isThrownBy(() -> sut.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(1L)))
            .withMessage("Unable to find report 1 of scenario 1");
    }

    @Test
    public void all_running_executions_are_set_to_KO_on_startup() {
        // Given running executions
        sut.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", ""));
        sut.store("2", buildDetachedExecution(ServerReportStatus.RUNNING, "exec2", ""));

        // When
        int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

        // Then, these executions are KO
        assertThat(nbOfAffectedExecutions).isEqualTo(2);
        assertThat(sut.getExecutions("1").get(0).status()).isEqualTo(ServerReportStatus.FAILURE);
        assertThat(sut.getExecutions("2").get(0).status()).isEqualTo(ServerReportStatus.FAILURE);

        // And there is no more running execution
        assertThat(sut.getExecutionsWithStatus(ServerReportStatus.RUNNING).size()).isEqualTo(0);
    }

    @Test
    public void getExecution_throws_when_not_found() {
        assertThatExceptionOfType(ReportNotFoundException.class)
            .isThrownBy(() -> sut.getExecution("-1", 42L))
            .withMessage("Unable to find report 42 of scenario -1");
    }

    @Test
    public void getExecution_throws_when_exist_but_not_on_this_scenario() {
        Execution executionCreated = sut.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", ""));

        assertThat(sut.getExecution("1", executionCreated.executionId())).isNotNull();

        assertThatExceptionOfType(ReportNotFoundException.class)
            .isThrownBy(() -> sut.getExecution("12345", executionCreated.executionId()))
            .withMessage("Unable to find report " + executionCreated.executionId() + " of scenario 12345");
    }

    @Test
    public void should_truncate_report_info_and_error_on_save_or_update() {
        final String tooLongString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede.";

        sut.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, tooLongString, tooLongString));

        assertThat(sut.getExecutions("1").get(0).info().get())
            .hasSize(512);

        assertThat(sut.getExecutions("1").get(0).error().get())
            .hasSize(512);
    }

    @Test
    public void should_map_campaign_only_when_executing_from_campaign() {
        // Given
        String scenarioId = "1";
        Execution exec1 = sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.FAILURE, "exec1", ""));
        Execution exec2 = sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));

        // scenario 1 was executed in campaign 1
        Long campaignId = saveNewCampaign();
        Long campaignExecutionId = 1L;
        saveCampaignExecutionReport(campaignId, campaignExecutionId, scenarioId, exec1);

        // When
        List<ExecutionSummary> executions = sut.getExecutions(scenarioId);

        // Then

        assertThat(executions).hasSize(2);
        assertThat(executions.get(0).executionId()).isEqualTo(exec2.executionId());
        assertThat(executions.get(0).campaignReport()).isEmpty();
        assertThat(executions.get(1).executionId()).isEqualTo(exec1.executionId());
        assertThat(executions.get(1).campaignReport()).hasValueSatisfying(report -> {
            assertThat(report.campaignId).isEqualTo(campaignId);
            assertThat(report.executionId).isEqualTo(campaignExecutionId);
        });
    }

    @Test
    public void should_retrieve_scenario_execution_summary() {
        // Given
        String scenarioId = "1";
        Execution exec1 = sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.FAILURE, "exec1", ""));
        sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));

        // scenario 1 was executed in campaign 1
        Long campaignId = saveNewCampaign();
        Long campaignExecutionId = 1L;
        saveCampaignExecutionReport(campaignId, campaignExecutionId, scenarioId, exec1);

        // When
        ExecutionSummary executionSummary = sut.getExecutionSummary(exec1.executionId());

        // Then


        assertThat(executionSummary.executionId()).isEqualTo(exec1.executionId());
        assertThat(executionSummary.campaignReport()).isPresent();
        assertThat(executionSummary.campaignReport().get().campaignId).isEqualTo(campaignId);
        assertThat(executionSummary.campaignReport().get().executionId).isEqualTo(campaignExecutionId);
    }

    private void saveCampaignExecutionReport(Long campaignId, Long campaignExecutionId, String scenarioId, Execution exec1) {
        ExecutionSummary execution = ImmutableExecutionHistory.ExecutionSummary.builder().executionId(exec1.executionId())
            .duration(3L)
            .time(LocalDateTime.now())
            .status(exec1.status())
            .testCaseTitle("fake")
            .environment("default")
            .datasetId("#2:87")
            .datasetVersion(5)
            .user("user")
            .build();
        ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenarioId, "scenario 1", execution);
        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaignId, singletonList(scenarioExecutionReport), "title", false, "env", "#2:87", 5, "user");

        campaignRepository.saveReport(campaignId, campaignExecutionReport);
    }

    private Long saveNewCampaign() {
        Campaign campaign = new Campaign(null, "test", "test campaign", newArrayList("1", "2"), null, "env", false, false, null, null);
        return campaignRepository.createOrUpdate(campaign).id;
    }

    private DetachedExecution buildDetachedExecution(ServerReportStatus status, String info, String error) {
        return ImmutableExecutionHistory.DetachedExecution.builder()
            .time(LocalDateTime.now())
            .duration(12L)
            .status(status)
            .info(info)
            .error(error)
            .report("report content")
            .testCaseTitle("Fake title")
            .environment("")
            .datasetId("fake dataset id")
            .user("")
            .build();
    }

    private void initCampaignRepository() {
        TestCaseRepositoryAggregator testCaseRepositoryMock = mock(TestCaseRepositoryAggregator.class);
        CampaignExecutionReportMapper campaignExecutionReportMapper = new CampaignExecutionReportMapper(testCaseRepositoryMock);
        TestCase mockTestCase = mock(TestCase.class);
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.title()).thenReturn("scenario title");
        when(mockTestCase.metadata()).thenReturn(mockTestCaseMetadata);
        when(testCaseRepositoryMock.findById(any())).thenReturn(of(mockTestCase));
        CampaignExecutionRepository campaignExecutionRepository = new CampaignExecutionRepository(namedParameterJdbcTemplate, campaignExecutionReportMapper);

        CampaignParameterRepository campaignParameterRepository = new CampaignParameterRepository(namedParameterJdbcTemplate);
        campaignRepository = new DatabaseCampaignRepository(namedParameterJdbcTemplate, campaignExecutionRepository, campaignParameterRepository);
    }
}

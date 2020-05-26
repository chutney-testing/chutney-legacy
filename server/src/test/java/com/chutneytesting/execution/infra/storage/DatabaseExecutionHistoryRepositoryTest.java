package com.chutneytesting.execution.infra.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.execution.domain.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.execution.domain.history.ExecutionHistory.Execution;
import com.chutneytesting.execution.domain.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.history.ReportNotFoundException;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class DatabaseExecutionHistoryRepositoryTest extends AbstractLocalDatabaseTest {

    private final ExecutionHistoryRepository executionHistoryRepository;

    public DatabaseExecutionHistoryRepositoryTest() {
        this.executionHistoryRepository = new DatabaseExecutionHistoryRepository(namedParameterJdbcTemplate);
    }

    @Test
    public void repository_is_empty_at_startup() {
        assertThat(executionHistoryRepository.getExecutions("1")).isEmpty();
    }

    @Test
    public void execution_summary_is_available_after_storing_sorted_newest_first() {
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec1", ""));
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.FAILURE, "exec3", ""));

        assertThat(executionHistoryRepository.getExecutions("1"))
            .extracting(summary -> summary.info().get()).containsExactly("exec3", "exec2", "exec1");
    }

    @Test
    public void execution_summaries_retrieved_are_limit_to_5() {
        IntStream.range(0, 25).forEach(
            i -> {
                executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec" + i, ""));

                // As order is based on executionTime, if they are stored at the exact same time, check on order may fail
                try {
                    TimeUnit.MILLISECONDS.sleep(20L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        );

        assertThat(executionHistoryRepository.getExecutions("1"))
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
            .build();
        IntStream.range(0, 23).forEach(i -> executionHistoryRepository.store("1", execution));

        assertThat(executionHistoryRepository.getExecutions("2")).hasSize(0);

        Map<String, Object> queryForMap = namedParameterJdbcTemplate.queryForMap(
            "SELECT count(*) as count FROM SCENARIO_EXECUTION_HISTORY WHERE SCENARIO_ID = 1"
            , new HashMap<>());

        assertThat(queryForMap.get("count"))
            .as("All 23 reports of test scenario")
            .isEqualTo(23L);
    }

    @Test
    public void update_execution_alters_last_one() {
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec", ""));

        ExecutionSummary last = executionHistoryRepository.getExecutions("1").get(0);
        assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
        assertThat(last.info()).contains("exec");

        executionHistoryRepository.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

        Execution updatedExecution = executionHistoryRepository.getExecution("1", last.executionId());
        assertThat(updatedExecution.status()).isEqualTo(ServerReportStatus.SUCCESS);
        assertThat(updatedExecution.info()).contains("updated");
    }

    @Test
    public void update_preserve_other_executions_order() {
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.FAILURE, "exec1", ""));
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec3", ""));

        ExecutionSummary last = executionHistoryRepository.getExecutions("1").get(0);
        assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
        assertThat(last.info()).contains("exec3");

        executionHistoryRepository.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

        assertThat(executionHistoryRepository.getExecutions("1"))
            .extracting(summary -> summary.info().get())
            .containsExactly(
                "updated",
                "exec2",
                "exec1");
    }

    @Test
    public void update_on_empty_history_throws() {
        assertThatExceptionOfType(ReportNotFoundException.class)
            .isThrownBy(() -> executionHistoryRepository.update("1", buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(1L)))
            .withMessage("Unable to find report 1 of scenario 1");
    }

    @Test
    public void all_running_executions_are_set_to_KO_on_startup() {
        // Given running executions
        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", ""));
        executionHistoryRepository.store("2", buildDetachedExecution(ServerReportStatus.RUNNING, "exec2", ""));

        // When
        int nbOfAffectedExecutions = executionHistoryRepository.setAllRunningExecutionsToKO();

        // Then, these executions are KO
        assertThat(nbOfAffectedExecutions).isEqualTo(2);
        assertThat(executionHistoryRepository.getExecutions("1").get(0).status()).isEqualTo(ServerReportStatus.FAILURE);
        assertThat(executionHistoryRepository.getExecutions("2").get(0).status()).isEqualTo(ServerReportStatus.FAILURE);

        // And there is no more running execution
        assertThat(executionHistoryRepository.getExecutionsWithStatus(ServerReportStatus.RUNNING).size()).isEqualTo(0);
    }

    @Test
    public void getExecution_throws_when_not_found() {
        assertThatExceptionOfType(ReportNotFoundException.class)
            .isThrownBy(() -> executionHistoryRepository.getExecution("-1", 42L))
            .withMessage("Unable to find report 42 of scenario -1");
    }

    @Test
    public void should_truncate_report_info_and_error_on_save_or_update() {
        final String tooLongString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede.";

        executionHistoryRepository.store("1", buildDetachedExecution(ServerReportStatus.SUCCESS, tooLongString, tooLongString));

        assertThat(executionHistoryRepository.getExecutions("1").get(0).info().get())
            .hasSize(512);

        assertThat(executionHistoryRepository.getExecutions("1").get(0).error().get())
            .hasSize(512);
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
            .build();
    }
}

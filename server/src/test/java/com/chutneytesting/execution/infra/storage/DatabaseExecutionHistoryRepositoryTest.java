package com.chutneytesting.execution.infra.storage;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;
import static util.WaitUtils.awaitDuring;

import com.chutneytesting.campaign.infra.CampaignExecutionDBRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionReportEntity;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.sqlite.SQLiteException;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;
import util.infra.EnablePostgreSQLTestInfra;
import util.infra.EnableSQLiteTestInfra;

public class DatabaseExecutionHistoryRepositoryTest {

    @Nested
    @EnableH2MemTestInfra
    class H2 extends AllTests {
    }

    @Nested
    @EnableSQLiteTestInfra
    class SQLite extends AllTests {
    }

    @Nested
    @EnablePostgreSQLTestInfra
    class PostreSQL extends AllTests {
    }

    abstract class AllTests extends AbstractLocalDatabaseTest {
        @Autowired
        private DatabaseExecutionHistoryRepository sut;
        @Autowired
        private CampaignExecutionDBRepository campaignExecutionDBRepository;

        private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        @Autowired
        private ScenarioExecutionReportJpaRepository scenarioExecutionReportJpaRepository;

        @Test
        public void parallel_execution_does_not_lock_database() throws InterruptedException {
            int numThreads = 10;
            // Given n parallels scenarios
            List<String> ids = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++) {
                String id = givenScenario().getId().toString();
                ids.add(id);
                sut.store(id, buildDetachedExecution(ServerReportStatus.RUNNING, "exec", ""));
            }

            // Use a latch to sync all threads
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(numThreads);

            List<Throwable> throwns = new ArrayList<>(numThreads);
            ids.forEach(((id) -> {
                ExecutionSummary summary = sut.getExecutions(id).get(0);
                Thread t = new Thread(() -> {
                    try {
                        startLatch.await();
                        throwns.add(catchThrowable(() ->
                            sut.update(id, buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(summary.executionId()))
                        ));
                    } catch (InterruptedException e) {
                        // do nothing
                    } finally {
                        endLatch.countDown();
                    }
                });
                t.start();
            }));

            startLatch.countDown(); // Starts all threads
            endLatch.await(); // await termination

            assertThat(throwns).doesNotHaveAnyElementsOfTypes(
                CannotAcquireLockException.class,
                LockAcquisitionException.class,
                SQLiteException.class
            );
        }

        @Test
        public void execution_summary_is_available_after_storing_sorted_newest_first() {
            String scenarioId = givenScenarioId(true);
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec1", ""));
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.FAILURE, "exec3", ""));

            assertThat(sut.getExecutions(scenarioId))
                .extracting(summary -> summary.info().get()).containsExactly("exec3", "exec2", "exec1");
        }

        @Test
        public void last_execution_return_newest_first() {
            String scenarioIdOne = givenScenarioId();
            sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec1", ""));
            sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
            sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.FAILURE, "exec3", ""));

            String scenarioIdTwo = givenScenarioId(true);
            sut.store(scenarioIdTwo, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec6", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec5", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(ServerReportStatus.FAILURE, "exec4", ""));

            Map<String, ExecutionSummary> lastExecutions = sut.getLastExecutions(List.of(scenarioIdOne, scenarioIdTwo));
            assertThat(lastExecutions).containsOnlyKeys(scenarioIdOne, scenarioIdTwo);
            assertThat(lastExecutions.get(scenarioIdOne).info()).hasValue("exec3");
            assertThat(lastExecutions.get(scenarioIdTwo).info()).hasValue("exec4");
        }

        @Test
        public void execution_summaries_retrieved_are_limit_to_20() {
            String scenarioId = givenScenarioId();
            List<String> expectedInfos = new ArrayList<>(20);
            List<String> finalExpectedInfos = expectedInfos;
            IntStream.range(0, 25).forEach(
                i -> {
                    String info = "exec" + i;
                    sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, info, ""));
                    finalExpectedInfos.add(info);
                    // As order is based on executionTime, if they are stored at the exact same time, check on order may fail
                    awaitDuring(20, MILLISECONDS);
                }
            );

            Collections.reverse(expectedInfos);
            expectedInfos = expectedInfos.stream().limit(20).toList();

            assertThat(sut.getExecutions(scenarioId))
                .extracting(ExecutionHistory.ExecutionProperties::info)
                .map(Optional::get)
                .containsExactlyElementsOf(expectedInfos);
        }

        @Test
        public void storage_keeps_all_items() {
            String scenarioId = givenScenarioId();
            DetachedExecution execution = buildDetachedExecution(ServerReportStatus.SUCCESS, "", "");
            IntStream.range(0, 23).forEach(i -> sut.store(scenarioId, execution));

            assertThat(sut.getExecutions("-1")).hasSize(0);

            Number executionsCount = (Number) entityManager.createNativeQuery(
                "SELECT count(*) as count FROM SCENARIO_EXECUTIONS WHERE SCENARIO_ID = '" + scenarioId + "'").getSingleResult();

            assertThat(executionsCount.intValue())
                .as("All 23 reports of test scenario")
                .isEqualTo(23);
        }

        @Test
        public void update_execution_alters_last_one() {
            String scenarioId = givenScenario().getId().toString();
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.RUNNING, "exec", ""));

            ExecutionSummary last = sut.getExecutions(scenarioId).get(0);
            assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
            assertThat(last.info()).hasValue("exec");

            sut.update(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

            Execution updatedExecution = sut.getExecution(scenarioId, last.executionId());
            assertThat(updatedExecution.status()).isEqualTo(ServerReportStatus.SUCCESS);
            assertThat(updatedExecution.info()).hasValue("updated");
        }

        @Test
        public void update_preserve_other_executions_order() {
            String scenarioId = givenScenarioId();
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.FAILURE, "exec1", ""));
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "exec2", ""));
            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.RUNNING, "exec3", ""));

            ExecutionSummary last = sut.getExecutions(scenarioId).get(0);
            assertThat(last.status()).isEqualTo(ServerReportStatus.RUNNING);
            assertThat(last.info()).contains("exec3");

            sut.update(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(last.executionId()));

            assertThat(
                sut.getExecutions(scenarioId).stream()
                    .map(ExecutionHistory.ExecutionProperties::info)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
            ).containsExactly("updated", "exec2", "exec1");
        }

        @Test
        public void update_on_empty_history_throws() {
            String scenarioId = givenScenarioId();
            long unknownExecutionId = -1L;
            assertThatExceptionOfType(ReportNotFoundException.class)
                .isThrownBy(() -> sut.update(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, "updated", "").attach(unknownExecutionId)))
                .withMessage("Unable to find report " + unknownExecutionId + " of scenario " + scenarioId);
        }

        @Test
        public void all_running_executions_are_set_to_KO_on_startup() {
            // Given running executions
            clearTables();
            String scenarioIdOne = givenScenarioId(true);
            String scenarioIdTwo = givenScenarioId();
            sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(ServerReportStatus.RUNNING, "exec2", ""));

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(2);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(ServerReportStatus.FAILURE);
            assertThat(sut.getExecutions(scenarioIdTwo).get(0).status()).isEqualTo(ServerReportStatus.FAILURE);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(ServerReportStatus.RUNNING).size()).isEqualTo(0);
        }

        @Test
        public void all_running_executions_are_set_to_KO_on_startup_check_report_status_update() throws JsonProcessingException {
            // Given running executions
            clearTables();
            String scenarioIdOne = "123";
            Long scenarioId = sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", "")).summary().executionId();

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(1);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(ServerReportStatus.FAILURE);
            ScenarioExecutionReportEntity scenarioExecutionReport = scenarioExecutionReportJpaRepository.findById(scenarioId).orElseThrow();
            com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport report = objectMapper.readValue(scenarioExecutionReport.getReport(), com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport.class);
            assertThat(report.report.status).isEqualTo(ServerReportStatus.SUCCESS);
            assertThat(report.report.steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).status).isEqualTo(ServerReportStatus.STOPPED);
            assertThat(report.report.steps.get(0).steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).steps.get(0).status).isEqualTo(ServerReportStatus.STOPPED);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(ServerReportStatus.RUNNING).size()).isEqualTo(0);
        }

        @Test
        public void all_paused_executions_are_set_to_KO_on_startup_check_report_status_update() throws JsonProcessingException {
            // Given running executions
            clearTables();
            String scenarioIdOne = "123";
            Long scenarioId = sut.store(scenarioIdOne, buildDetachedExecution(ServerReportStatus.PAUSED, "exec1", "")).summary().executionId();

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(1);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(ServerReportStatus.FAILURE);
            ScenarioExecutionReportEntity scenarioExecutionReport = scenarioExecutionReportJpaRepository.findById(scenarioId).orElseThrow();
            com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport report = objectMapper.readValue(scenarioExecutionReport.getReport(), com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport.class);
            assertThat(report.report.status).isEqualTo(ServerReportStatus.SUCCESS);
            assertThat(report.report.steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).status).isEqualTo(ServerReportStatus.STOPPED);
            assertThat(report.report.steps.get(0).steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).steps.get(0).status).isEqualTo(ServerReportStatus.STOPPED);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(ServerReportStatus.PAUSED).size()).isEqualTo(0);
        }

        @Test
        public void getExecution_throws_when_not_found() {
            assertThatExceptionOfType(ReportNotFoundException.class)
                .isThrownBy(() -> sut.getExecution("-1", 42L))
                .withMessage("Unable to find report 42 of scenario -1");
        }

        @Test
        public void getExecution_throws_when_exist_but_not_on_this_scenario() {
            String scenarioId = givenScenario().getId().toString();
            Execution executionCreated = sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.RUNNING, "exec1", ""));

            assertThat(sut.getExecution(scenarioId, executionCreated.executionId())).isNotNull();

            assertThatExceptionOfType(ReportNotFoundException.class)
                .isThrownBy(() -> sut.getExecution("-1", executionCreated.executionId()))
                .withMessage("Unable to find report " + executionCreated.executionId() + " of scenario -1");
        }

        @Test
        public void should_truncate_report_info_and_error_on_save_or_update() {
            String scenarioId = givenScenarioId(true);
            final String tooLongString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede.";

            sut.store(scenarioId, buildDetachedExecution(ServerReportStatus.SUCCESS, tooLongString, tooLongString));

            assertThat(sut.getExecutions(scenarioId).get(0).info())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));

            assertThat(sut.getExecutions(scenarioId).get(0).error())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));
        }

        @Test
        public void should_map_campaign_only_when_executing_from_campaign() {
            // Given
            Scenario scenario = givenScenario();
            CampaignEntity campaign = givenCampaign(scenario);

            ScenarioExecutionEntity scenarioExecutionOne = givenScenarioExecution(scenario.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioExecutionOneReport = new ScenarioExecutionReportCampaign(scenario.getId().toString(), scenario.getTitle(), scenarioExecutionOne.toDomain());
            ScenarioExecutionEntity scenarioExecutionTwo = givenScenarioExecution(scenario.getId(), ServerReportStatus.SUCCESS);

            Long campaignExecutionId = campaignExecutionDBRepository.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "#2:87", 5, "user");
            campaignExecutionDBRepository.saveCampaignReport(campaign.id(), campaignExecutionReport);

            // When
            List<ExecutionSummary> executions = sut.getExecutions(scenario.getId().toString());

            // Then
            assertThat(executions).hasSize(2);
            assertThat(executions.get(0).executionId()).isEqualTo(scenarioExecutionTwo.id());
            assertThat(executions.get(0).campaignReport()).isEmpty();
            assertThat(executions.get(1).executionId()).isEqualTo(scenarioExecutionOne.id());
            assertThat(executions.get(1).campaignReport()).hasValueSatisfying(report -> {
                assertThat(report.campaignId).isEqualTo(campaign.id());
                assertThat(report.executionId).isEqualTo(campaignExecutionId);
            });
        }

        @Test
        public void should_retrieve_scenario_execution_summary() {
            // Given
            Scenario scenario = givenScenario();
            CampaignEntity campaign = givenCampaign(scenario);

            ScenarioExecutionEntity scenarioExecutionOne = givenScenarioExecution(scenario.getId(), ServerReportStatus.FAILURE);
            ScenarioExecutionReportCampaign scenarioExecutionOneReport = new ScenarioExecutionReportCampaign(scenario.getId().toString(), scenario.getTitle(), scenarioExecutionOne.toDomain());
            givenScenarioExecution(scenario.getId(), ServerReportStatus.SUCCESS);

            Long campaignExecutionId = campaignExecutionDBRepository.generateCampaignExecutionId(campaign.id());
            CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "#2:87", 5, "user");
            campaignExecutionDBRepository.saveCampaignReport(campaign.id(), campaignExecutionReport);

            // When
            ExecutionSummary executionSummary = sut.getExecutionSummary(scenarioExecutionOne.id());

            // Then
            assertThat(executionSummary.executionId()).isEqualTo(scenarioExecutionOne.id());
            assertThat(executionSummary.campaignReport()).isPresent();
            assertThat(executionSummary.campaignReport()).hasValueSatisfying(cr -> {
                assertThat(cr.campaignId).isEqualTo(campaign.id());
                assertThat(cr.executionId).isEqualTo(campaignExecutionId);
            });
        }

        private DetachedExecution buildDetachedExecution(ServerReportStatus status, String info, String error) {
            return ImmutableExecutionHistory.DetachedExecution.builder()
                .time(LocalDateTime.now())
                .duration(12L)
                .status(status)
                .info(info)
                .error(error)
                .report(buildReport())
                .testCaseTitle("Fake title")
                .environment("")
                .datasetId("fake dataset id")
                .user("")
                .build();
        }

        private String buildReport() {
            StepExecutionReportCore successStepReport =
                stepReport("root step Title", -1L, ServerReportStatus.SUCCESS,
                    stepReport("step 1", 24L, ServerReportStatus.PAUSED,
                        stepReport("step1.1", 23L, ServerReportStatus.RUNNING)));
            try {
                return objectMapper.writeValueAsString(new ScenarioExecutionReport(1L, "scenario name", "", "", successStepReport));
            } catch (JsonProcessingException exception) {
                return "";
            }
        }

        private StepExecutionReportCore stepReport(String title, long duration, ServerReportStatus status, StepExecutionReportCore... subSteps) {
            List<String> infos = ServerReportStatus.SUCCESS == status ? singletonList("test info") : emptyList();
            List<String> errors = ServerReportStatus.FAILURE == status ? singletonList("test error") : emptyList();

            return new StepExecutionReportCore(
                title,
                duration,
                Instant.now(),
                status,
                infos,
                errors,
                Arrays.asList(subSteps),
                "type",
                "targetName",
                "targetUrl",
                "strategy",
                Maps.newHashMap(),
                Maps.newHashMap());
        }
    }
}

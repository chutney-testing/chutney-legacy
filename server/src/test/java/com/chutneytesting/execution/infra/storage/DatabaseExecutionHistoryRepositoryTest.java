/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.execution.infra.storage;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.FAILURE;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.NOT_EXECUTED;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.PAUSED;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.RUNNING;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.STOPPED;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.chutneytesting.campaign.infra.CampaignExecutionDBRepository;
import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionReportEntity;
import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
import com.chutneytesting.scenario.infra.raw.DatabaseTestCaseRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.NestedTestConfiguration;
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

        @Autowired
        @Qualifier("reportObjectMapper")
        private ObjectMapper objectMapper;

        @Autowired
        private ScenarioExecutionReportJpaRepository scenarioExecutionReportJpaRepository;

        @Autowired
        private DatabaseTestCaseRepository databaseTestCaseRepository;

        @Test
        public void parallel_execution_does_not_lock_database() throws InterruptedException {
            int numThreads = 10;
            // Given n parallels scenarios
            List<String> ids = new ArrayList<>(numThreads);
            for (int i = 0; i < numThreads; i++) {
                String id = givenScenario().getId().toString();
                ids.add(id);
                sut.store(id, buildDetachedExecution(RUNNING, "exec", ""));
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
                            sut.update(id, buildDetachedExecution(SUCCESS, "updated", "").attach(summary.executionId(), id))
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
            sut.store(scenarioId, buildDetachedExecution(SUCCESS, "exec1", ""));
            sut.store(scenarioId, buildDetachedExecution(SUCCESS, "exec2", ""));
            sut.store(scenarioId, buildDetachedExecution(FAILURE, "exec3", ""));

            assertThat(sut.getExecutions(scenarioId))
                .extracting(summary -> summary.info().get()).containsExactly("exec3", "exec2", "exec1");
        }

        @Test
        public void last_execution_return_newest_first() {
            String scenarioIdOne = givenScenarioId();
            sut.store(scenarioIdOne, buildDetachedExecution(SUCCESS, "exec1", ""));
            sut.store(scenarioIdOne, buildDetachedExecution(SUCCESS, "exec2", ""));
            sut.store(scenarioIdOne, buildDetachedExecution(FAILURE, "exec3", ""));

            String scenarioIdTwo = givenScenarioId(true);
            sut.store(scenarioIdTwo, buildDetachedExecution(SUCCESS, "exec6", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(SUCCESS, "exec5", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(FAILURE, "exec4", ""));

            Map<String, ExecutionSummary> lastExecutions = sut.getLastExecutions(List.of(scenarioIdOne, scenarioIdTwo));
            assertThat(lastExecutions).containsOnlyKeys(scenarioIdOne, scenarioIdTwo);
            assertThat(lastExecutions.get(scenarioIdOne).info()).hasValue("exec3");
            assertThat(lastExecutions.get(scenarioIdTwo).info()).hasValue("exec4");
        }

        @Test
        public void last_execution_does_not_return_not_executed() {
            String scenarioIdOne = givenScenarioId();
            sut.store(scenarioIdOne, buildDetachedExecution(SUCCESS, "exec1", ""));
            sut.store(scenarioIdOne, buildDetachedExecution(NOT_EXECUTED, "exec2", ""));

            Map<String, ExecutionSummary> lastExecutions = sut.getLastExecutions(List.of(scenarioIdOne, scenarioIdOne));
            assertThat(lastExecutions).containsOnlyKeys(scenarioIdOne);
            assertThat(lastExecutions.get(scenarioIdOne).info()).hasValue("exec1");
            assertThat(lastExecutions.get(scenarioIdOne).status()).isEqualTo(SUCCESS);
        }

        @Test
        public void storage_keeps_all_items() {
            String scenarioId = givenScenarioId();
            DetachedExecution execution = buildDetachedExecution(SUCCESS, "", "");
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
            sut.store(scenarioId, buildDetachedExecution(RUNNING, "exec", ""));

            ExecutionSummary last = sut.getExecutions(scenarioId).get(0);
            assertThat(last.status()).isEqualTo(RUNNING);
            assertThat(last.info()).hasValue("exec");

            sut.update(scenarioId, buildDetachedExecution(SUCCESS, "updated", "").attach(last.executionId(), scenarioId));

            Execution updatedExecution = sut.getExecution(scenarioId, last.executionId());
            assertThat(updatedExecution.status()).isEqualTo(SUCCESS);
            assertThat(updatedExecution.info()).hasValue("updated");
        }

        @Ignore("TODO - Failed sometimes - investigation has to be done")
        @Test
        public void update_preserve_other_executions_order() {
            String scenarioId = givenScenarioId();
            sut.store(scenarioId, buildDetachedExecution(FAILURE, "exec1", ""));
            sut.store(scenarioId, buildDetachedExecution(SUCCESS, "exec2", ""));
            sut.store(scenarioId, buildDetachedExecution(RUNNING, "exec3", ""));

            ExecutionSummary last = sut.getExecutions(scenarioId).get(0);
            assertThat(last.status()).isEqualTo(RUNNING);
            assertThat(last.info()).contains("exec3");

            sut.update(scenarioId, buildDetachedExecution(SUCCESS, "updated", "").attach(last.executionId(), scenarioId));

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
                .isThrownBy(() -> sut.update(scenarioId, buildDetachedExecution(SUCCESS, "updated", "").attach(unknownExecutionId, scenarioId)))
                .withMessage("Unable to find report " + unknownExecutionId + " of scenario " + scenarioId);
        }

        @Test
        public void all_running_executions_are_set_to_KO_on_startup() {
            // Given running executions
            clearTables();
            String scenarioIdOne = givenScenarioId(true);
            String scenarioIdTwo = givenScenarioId();
            sut.store(scenarioIdOne, buildDetachedExecution(RUNNING, "exec1", ""));
            sut.store(scenarioIdTwo, buildDetachedExecution(RUNNING, "exec2", ""));

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(2);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(FAILURE);
            assertThat(sut.getExecutions(scenarioIdTwo).get(0).status()).isEqualTo(FAILURE);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(RUNNING).size()).isEqualTo(0);
        }

        @Test
        public void all_running_executions_are_set_to_KO_on_startup_check_report_status_update() throws JsonProcessingException {
            // Given running executions
            clearTables();
            String scenarioIdOne = "123";
            Long scenarioId = sut.store(scenarioIdOne, buildDetachedExecution(RUNNING, "exec1", "")).summary().executionId();

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(1);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(FAILURE);
            ScenarioExecutionReportEntity scenarioExecutionReport = scenarioExecutionReportJpaRepository.findById(scenarioId).orElseThrow();
            ScenarioExecutionReport report = objectMapper.readValue(scenarioExecutionReport.getReport(), ScenarioExecutionReport.class);
            assertThat(report.report.status).isEqualTo(SUCCESS);
            assertThat(report.report.steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).status).isEqualTo(STOPPED);
            assertThat(report.report.steps.get(0).steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).steps.get(0).status).isEqualTo(STOPPED);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(RUNNING).size()).isEqualTo(0);
        }

        @Test
        public void all_paused_executions_are_set_to_KO_on_startup_check_report_status_update() throws JsonProcessingException {
            // Given running executions
            clearTables();
            String scenarioIdOne = "123";
            Long scenarioId = sut.store(scenarioIdOne, buildDetachedExecution(PAUSED, "exec1", "")).summary().executionId();

            // When
            int nbOfAffectedExecutions = sut.setAllRunningExecutionsToKO();

            // Then, these executions are KO
            assertThat(nbOfAffectedExecutions).isEqualTo(1);
            assertThat(sut.getExecutions(scenarioIdOne).get(0).status()).isEqualTo(FAILURE);
            ScenarioExecutionReportEntity scenarioExecutionReport = scenarioExecutionReportJpaRepository.findById(scenarioId).orElseThrow();
            ScenarioExecutionReport report = objectMapper.readValue(scenarioExecutionReport.getReport(), ScenarioExecutionReport.class);
            assertThat(report.report.status).isEqualTo(SUCCESS);
            assertThat(report.report.steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).status).isEqualTo(STOPPED);
            assertThat(report.report.steps.get(0).steps.size()).isEqualTo(1);
            assertThat(report.report.steps.get(0).steps.get(0).status).isEqualTo(STOPPED);

            // And there is no more running execution
            assertThat(sut.getExecutionsWithStatus(PAUSED).size()).isEqualTo(0);
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
            Execution executionCreated = sut.store(scenarioId, buildDetachedExecution(RUNNING, "exec1", ""));

            assertThat(sut.getExecution(scenarioId, executionCreated.executionId())).isNotNull();

            assertThatExceptionOfType(ReportNotFoundException.class)
                .isThrownBy(() -> sut.getExecution("-1", executionCreated.executionId()))
                .withMessage("Unable to find report " + executionCreated.executionId() + " of scenario -1");
        }

        @Test
        public void should_truncate_report_info_and_error_on_save_or_update() {
            String scenarioId = givenScenarioId(true);
            final String tooLongString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi. Proin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim. Pellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede.";

            Execution last = sut.store(scenarioId, buildDetachedExecution(SUCCESS, tooLongString, tooLongString));

            assertThat(sut.getExecutions(scenarioId).get(0).info())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));

            assertThat(sut.getExecutions(scenarioId).get(0).error())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));

            sut.update(scenarioId, buildDetachedExecution(SUCCESS, tooLongString, tooLongString).attach(last.executionId(), scenarioId));

            assertThat(sut.getExecutions(scenarioId).get(0).info())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));

            assertThat(sut.getExecutions(scenarioId).get(0).error())
                .hasValueSatisfying(v -> assertThat(v).hasSize(512));
        }

        @Test
        public void should_map_campaign_only_when_executing_from_campaign() {
            // Given
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecutionOne = givenScenarioExecution(scenarioEntity.getId(), FAILURE);
            ScenarioExecutionCampaign scenarioExecutionOneReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionOne.toDomain());
            ScenarioExecutionEntity scenarioExecutionTwo = givenScenarioExecution(scenarioEntity.getId(), SUCCESS);

            Long campaignExecutionId = campaignExecutionDBRepository.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "#2:87", 5, "user");
            campaignExecutionDBRepository.saveCampaignExecution(campaign.id(), campaignExecution);

            // When
            List<ExecutionSummary> executions = sut.getExecutions(scenarioEntity.getId().toString());

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
            ScenarioEntity scenarioEntity = givenScenario();
            CampaignEntity campaign = givenCampaign(scenarioEntity);

            ScenarioExecutionEntity scenarioExecutionOne = givenScenarioExecution(scenarioEntity.getId(), FAILURE);
            ScenarioExecutionCampaign scenarioExecutionOneReport = new ScenarioExecutionCampaign(scenarioEntity.getId().toString(), scenarioEntity.getTitle(), scenarioExecutionOne.toDomain());
            givenScenarioExecution(scenarioEntity.getId(), SUCCESS);

            Long campaignExecutionId = campaignExecutionDBRepository.generateCampaignExecutionId(campaign.id(), "executionEnv");
            CampaignExecution campaignExecution = new CampaignExecution(campaignExecutionId, campaign.id(), singletonList(scenarioExecutionOneReport), campaign.title(), true, "env", "#2:87", 5, "user");
            campaignExecutionDBRepository.saveCampaignExecution(campaign.id(), campaignExecution);

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

        @Test
        void deletes_executions_by_ids() {
            String scenarioId = givenScenarioId();
            Execution exec1 = sut.store(scenarioId, buildDetachedExecution(SUCCESS, "exec1", ""));
            Execution exec2 = sut.store(scenarioId, buildDetachedExecution(SUCCESS, "exec2", ""));

            sut.deleteExecutions(Set.of(exec1.executionId(), exec2.executionId()));

            List.of(exec1.executionId(), exec2.executionId()).forEach(executionId -> assertThatThrownBy(() ->
                sut.getExecutionSummary(executionId)
            ).isInstanceOf(ReportNotFoundException.class));
        }

        @Nested
        @NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
        @DisplayName("Find scenario execution with report match")
        class ScenarioExecutionReportMatch {
            @Test
            void simple_case() {
                clearTables();
                var scenarioId1 = givenScenario().getId().toString();
                var scenarioId2 = givenScenario().getId().toString();
                var exec1 = sut.store(scenarioId1, buildDetachedExecution("toto"));
                sut.store(scenarioId2, buildDetachedExecution("tutu"));

                var executionSummaryList = sut.getExecutionReportMatchQuery("to");

                assertThat(executionSummaryList).hasSize(1);
                assertThat(executionSummaryList.get(0).executionId()).isEqualTo(exec1.executionId());
                assertThat(executionSummaryList.get(0).scenarioId()).isEqualTo(exec1.scenarioId());
            }

            @Test
            void filter_unactivated_scenario_execution() {
                clearTables();
                var scenarioId1 = givenScenario().getId().toString();
                var scenarioId2 = givenScenario().getId().toString();
                var exec1 = sut.store(scenarioId1, buildDetachedExecution("toto"));
                sut.store(scenarioId2, buildDetachedExecution("tutu"));
                databaseTestCaseRepository.removeById(scenarioId2);

                var executionSummaryList = sut.getExecutionReportMatchQuery("t");

                assertThat(executionSummaryList).hasSize(1);
                assertThat(executionSummaryList.get(0).executionId()).isEqualTo(exec1.executionId());
                assertThat(executionSummaryList.get(0).scenarioId()).isEqualTo(exec1.scenarioId());
            }

            @Test
            void limit_results_to_100() {
                clearTables();
                IntStream.range(0, 110).forEach(i -> {
                    String scenarioId = givenScenario().getId().toString();
                    sut.store(scenarioId, buildDetachedExecution("report"));
                });

                var executionSummaryList = sut.getExecutionReportMatchQuery("ort");

                assertThat(executionSummaryList).hasSize(100);
            }

            @Test
            void order_by_id_descending() {
                clearTables();
                List<Long> executionsIds = new ArrayList<>();
                IntStream.range(0, 10).forEach(i -> {
                    var scenarioId = givenScenario().getId();
                    var execution = sut.store(scenarioId.toString(), buildDetachedExecution("report"));
                    executionsIds.add(execution.executionId());
                });
                var expectedOrder = executionsIds.stream().sorted(Comparator.<Long>naturalOrder().reversed()).toList();

                var executionSummaryList = sut.getExecutionReportMatchQuery("ort");

                assertThat(executionSummaryList)
                    .map(ExecutionSummary::executionId)
                    .containsExactlyElementsOf(expectedOrder);
            }

            private DetachedExecution buildDetachedExecution(String report) {
                return ImmutableExecutionHistory.DetachedExecution.builder()
                    .time(LocalDateTime.now())
                    .duration(12L)
                    .status(SUCCESS)
                    .report(report)
                    .testCaseTitle("Fake title")
                    .environment("")
                    .datasetId("fake dataset id")
                    .user("")
                    .build();
            }
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
                stepReport("root step Title", -1L, SUCCESS,
                    stepReport("step 1", 24L, PAUSED,
                        stepReport("step1.1", 23L, RUNNING)));
            try {
                return objectMapper.writeValueAsString(new ScenarioExecutionReport(1L, "scenario name", "", "", successStepReport));
            } catch (JsonProcessingException exception) {
                return "";
            }
        }

        private StepExecutionReportCore stepReport(String title, long duration, ServerReportStatus status, StepExecutionReportCore... subSteps) {
            List<String> infos = SUCCESS == status ? singletonList("test info") : emptyList();
            List<String> errors = FAILURE == status ? singletonList("test error") : emptyList();

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

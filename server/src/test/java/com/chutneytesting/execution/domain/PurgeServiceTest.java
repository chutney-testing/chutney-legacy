package com.chutneytesting.execution.domain;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.FAILURE;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.PurgeService.PurgeReport;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignBuilder;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Purge service")
public class PurgeServiceTest {
    @Nested
    @DisplayName("deletes scenarios' executions according to configuration")
    class CleanScenariosExecutionsOutsideCampaign {
        @Nested
        @DisplayName("for one scenario")
        class OneScenario {
            @Nested
            @DisplayName("for one environment")
            class OneEnvironment {
                @Test
                void no_purge_when_limit_not_exceeded() {
                    // Given
                    // Two scenario's executions independents of any campaigns' execution
                    // And a configuration limit set to 2 for scenarios' executions
                    Integer maxScenarioExecutionsConfiguration = 2;
                    String scenarioId = "1";

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));
                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        executionBuilder().executionId(1L).build(),
                        executionBuilder().executionId(2L).build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The two scenario's executions are not deleted
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    assertThat(report.scenariosExecutionsIds()).isEmpty();
                }

                @Test
                void purge_oldest_when_limit_exceeded() {
                    // Given
                    // Three scenario's executions independents of any campaigns' execution
                    // And a configuration limit set to 2 for scenarios' executions
                    Integer maxScenarioExecutionsConfiguration = 2;
                    String scenarioId = "1";
                    Long oldestScenarioExecutionId = 1L;

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));
                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    LocalDateTime now = now();
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        executionBuilder().executionId(3L).time(now).build(),
                        executionBuilder().executionId(2L).time(now.minusSeconds(10)).build(),
                        executionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(20)).build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest scenario's execution is deleted
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    verify(executionsRepository).deleteExecutions(Set.of(oldestScenarioExecutionId));
                    assertThat(report.scenariosExecutionsIds()).containsExactly(oldestScenarioExecutionId);
                }
            }

            @Nested
            @DisplayName("for two different environments")
            class MultipleEnvironments {
                @Test
                void no_purge_when_limit_not_exceeded() {
                    // Given
                    // Two scenario's executions independents of any campaigns' execution
                    // And a configuration limit set to 1 for scenarios' executions
                    Integer maxScenarioExecutionsConfiguration = 1;
                    String scenarioId = "1";

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));
                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        executionBuilder().executionId(1L).environment("env1").build(),
                        executionBuilder().executionId(2L).environment("env2").build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The two scenario's executions are not deleted
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    assertThat(report.scenariosExecutionsIds()).isEmpty();
                }

                @Test
                void purge_oldest_when_limit_exceeded() {
                    // Given
                    // Three scenario's executions independents of any campaigns' execution on two different environments
                    // And a configuration limit set to 1 for scenarios' executions
                    Integer maxScenarioExecutionsConfiguration = 1;
                    LocalDateTime now = now();
                    String scenarioId = "1";
                    Long oldestScenarioExecutionId = 1L;

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));
                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        executionBuilder().executionId(3L).time(now).environment("env1").build(),
                        executionBuilder().executionId(2L).time(now.minusSeconds(10)).environment("env2").build(),
                        executionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(20)).environment("env1").build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest scenario's execution on the environment with two executions is deleted
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    verify(executionsRepository).deleteExecutions(Set.of(oldestScenarioExecutionId));
                    assertThat(report.scenariosExecutionsIds()).containsExactly(oldestScenarioExecutionId);
                }
            }
        }

        @Nested
        @DisplayName("for two scenarios")
        class TwoScenarios {
            @Test
            void purge_oldest_when_limit_exceeded() {
                // Given
                // Two scenarios and three scenario's executions independents of any campaigns' execution
                // And a configuration limit set to 1 for scenarios' executions
                Integer maxScenarioExecutionsConfiguration = 1;
                LocalDateTime now = now();
                String scenarioId1 = "1";
                String scenarioId2 = "2";

                TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                when(testCaseRepository.findAll()).thenReturn(List.of(
                    TestCaseMetadataImpl.builder().withId(scenarioId1).build(),
                    TestCaseMetadataImpl.builder().withId(scenarioId2).build()
                ));
                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                when(executionsRepository.getExecutions(scenarioId1)).thenReturn(List.of(
                    executionBuilder().executionId(1L).time(now).build(),
                    executionBuilder().executionId(3L).time(now.minusSeconds(10)).build()
                ));
                when(executionsRepository.getExecutions(scenarioId2)).thenReturn(List.of(
                    executionBuilder().executionId(2L).time(now.minusSeconds(20)).build()
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest scenario's execution is deleted for the scenario with two executions
                verify(testCaseRepository).findAll();
                verify(executionsRepository).getExecutions(scenarioId1);
                verify(executionsRepository).getExecutions(scenarioId2);
                verify(executionsRepository).deleteExecutions(Set.of(3L));
                assertThat(report.scenariosExecutionsIds()).containsExactly(3L);
            }
        }
    }

    @Nested
    @DisplayName("deletes campaigns' executions according to configuration")
    class CleanCampaignsExecutionsWithoutManualScenarioExecution {
        @Nested
        @DisplayName("for one campaign")
        class OneCampaign {
            @Nested
            @DisplayName("for one environment")
            class OneEnvironment {
                @Test
                void purge_oldest_when_limit_exceeded() {
                    // Given
                    // Three campaign's executions with only one scenario's execution each
                    // And a configuration limit set to 10 for scenarios' executions
                    // And a configuration limit set to 2 for campaigns' executions
                    Integer maxScenarioExecutionsConfiguration = 10;
                    Integer maxCampaignExecutionsConfiguration = 2;
                    String scenarioId = "1";
                    Long campaignId = 1L;
                    Long oldestCampaignExecutionId = 1L;
                    LocalDateTime now = now();

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));

                    ExecutionSummary scenarioExecution1 = executionBuilder().executionId(3L).time(now).build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
                    CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(3L, campaignId, List.of(scenarioExecutionReportCampaign1), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

                    ExecutionSummary scenarioExecution2 = executionBuilder().executionId(2L).time(now.minusSeconds(10)).build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
                    CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(2L, campaignId, List.of(scenarioExecutionReportCampaign2), "", false, scenarioExecution2.environment(), scenarioExecution2.datasetId().orElse(null), scenarioExecution2.datasetVersion().orElse(null), scenarioExecution2.user());

                    ExecutionSummary scenarioExecution3 = executionBuilder().executionId(1L).time(now.minusSeconds(20)).build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign3 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution3.testCaseTitle(), scenarioExecution3);
                    CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(oldestCampaignExecutionId, campaignId, List.of(scenarioExecutionReportCampaign3), "", false, scenarioExecution3.environment(), scenarioExecution3.datasetId().orElse(null), scenarioExecution3.datasetVersion().orElse(null), scenarioExecution3.user());

                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                        scenarioExecution2.withCampaignReport(campaignExecutionReport2),
                        scenarioExecution3.withCampaignReport(campaignExecutionReport3)
                    ));

                    CampaignRepository campaignRepository = mock(CampaignRepository.class);
                    when(campaignRepository.findAll()).thenReturn(List.of(
                        CampaignBuilder.builder().setId(campaignId).build()
                    ));
                    CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                    when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                        campaignExecutionReport1, campaignExecutionReport2, campaignExecutionReport3
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest campaign's execution is deleted
                    // The associated scenario's execution is kept
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    verify(executionsRepository, times(0)).deleteExecutions(anySet());
                    verify(campaignRepository).findAll();
                    verify(campaignRepository).findExecutionsById(campaignId);
                    verify(campaignExecutionRepository).deleteExecutions(Set.of(oldestCampaignExecutionId));
                    assertThat(report.scenariosExecutionsIds()).isEmpty();
                    assertThat(report.campaignsExecutionsIds()).containsExactly(oldestCampaignExecutionId);
                }
            }

            @Nested
            @DisplayName("for two environments")
            class TwoEnvironments {
                @Test
                void purge_oldest_when_limit_exceeded() {
                    // Given
                    // Three campaign's executions with only one scenario's execution each on two different environments
                    // And a configuration limit set to 10 for scenarios' executions
                    // And a configuration limit set to 1 for campaigns' executions
                    Integer maxScenarioExecutionsConfiguration = 10;
                    Integer maxCampaignExecutionsConfiguration = 1;
                    String scenarioId = "1";
                    Long campaignId = 1L;
                    Long oldestCampaignExecutionId = 1L;
                    LocalDateTime now = now();

                    TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                    when(testCaseRepository.findAll()).thenReturn(List.of(
                        TestCaseMetadataImpl.builder().withId(scenarioId).build()
                    ));

                    ExecutionSummary scenarioExecution1 = executionBuilder().executionId(3L).time(now).environment("env1").build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
                    CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(3L, campaignId, List.of(scenarioExecutionReportCampaign1), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

                    ExecutionSummary scenarioExecution2 = executionBuilder().executionId(2L).time(now.minusSeconds(10)).environment("env2").build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
                    CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(2L, campaignId, List.of(scenarioExecutionReportCampaign2), "", false, scenarioExecution2.environment(), scenarioExecution2.datasetId().orElse(null), scenarioExecution2.datasetVersion().orElse(null), scenarioExecution2.user());

                    ExecutionSummary scenarioExecution3 = executionBuilder().executionId(1L).time(now.minusSeconds(20)).environment("env1").build();
                    ScenarioExecutionReportCampaign scenarioExecutionReportCampaign3 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution3.testCaseTitle(), scenarioExecution3);
                    CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(oldestCampaignExecutionId, campaignId, List.of(scenarioExecutionReportCampaign3), "", false, scenarioExecution3.environment(), scenarioExecution3.datasetId().orElse(null), scenarioExecution3.datasetVersion().orElse(null), scenarioExecution3.user());

                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                        scenarioExecution2.withCampaignReport(campaignExecutionReport2),
                        scenarioExecution3.withCampaignReport(campaignExecutionReport3)
                    ));

                    CampaignRepository campaignRepository = mock(CampaignRepository.class);
                    when(campaignRepository.findAll()).thenReturn(List.of(
                        CampaignBuilder.builder().setId(campaignId).build()
                    ));
                    CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                    when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                        campaignExecutionReport1, campaignExecutionReport2, campaignExecutionReport3
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest campaign's execution on the environment with two executions is deleted
                    // The associated scenario's execution is kept
                    verify(testCaseRepository).findAll();
                    verify(executionsRepository).getExecutions(scenarioId);
                    verify(executionsRepository, times(0)).deleteExecutions(anySet());
                    verify(campaignRepository).findAll();
                    verify(campaignRepository).findExecutionsById(campaignId);
                    verify(campaignExecutionRepository).deleteExecutions(Set.of(oldestCampaignExecutionId));
                    assertThat(report.scenariosExecutionsIds()).isEmpty();
                    assertThat(report.campaignsExecutionsIds()).containsExactly(oldestCampaignExecutionId);
                }
            }
        }

        @Nested
        @DisplayName("for two campaigns")
        class TwoCampaigns {
            @Test
            void purge_oldest_when_limit_exceeded() {
                // Given
                // Two campaigns with three campaign's executions each with only one scenario's execution each
                // And a configuration limit set to 10 for scenarios' executions
                // And a configuration limit set to 2 for campaigns' executions
                Integer maxScenarioExecutionsConfiguration = 10;
                Integer maxCampaignExecutionsConfiguration = 2;
                String scenarioId = "1";
                Long campaignId1 = 1L;
                Long campaignId2 = 2L;
                Long oldestCampaignExecutionId1 = 2L;
                Long oldestCampaignExecutionId2 = 1L;
                LocalDateTime now = now();

                TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                when(testCaseRepository.findAll()).thenReturn(List.of(
                    TestCaseMetadataImpl.builder().withId(scenarioId).build()
                ));

                // Scenario executions
                ExecutionSummary scenarioExecution1 = executionBuilder().executionId(6L).time(now).build();
                ExecutionSummary scenarioExecution2 = executionBuilder().executionId(5L).time(now.minusSeconds(10)).build();
                ExecutionSummary scenarioExecution3 = executionBuilder().executionId(4L).time(now.minusSeconds(20)).build();
                ExecutionSummary scenarioExecution4 = executionBuilder().executionId(3L).time(now.minusSeconds(30)).build();
                ExecutionSummary scenarioExecution5 = executionBuilder().executionId(2L).time(now.minusSeconds(40)).build();
                ExecutionSummary scenarioExecution6 = executionBuilder().executionId(1L).time(now.minusSeconds(50)).build();

                // First campaign executions
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
                CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(6L, campaignId1, List.of(scenarioExecutionReportCampaign1), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution3.testCaseTitle(), scenarioExecution3);
                CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(4L, campaignId1, List.of(scenarioExecutionReportCampaign2), "", false, scenarioExecution3.environment(), scenarioExecution3.datasetId().orElse(null), scenarioExecution3.datasetVersion().orElse(null), scenarioExecution3.user());

                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign3 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution5.testCaseTitle(), scenarioExecution5);
                CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(oldestCampaignExecutionId1, campaignId1, List.of(scenarioExecutionReportCampaign3), "", false, scenarioExecution5.environment(), scenarioExecution5.datasetId().orElse(null), scenarioExecution5.datasetVersion().orElse(null), scenarioExecution5.user());

                // Second campaign executions
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign4 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
                CampaignExecutionReport campaignExecutionReport4 = new CampaignExecutionReport(5L, campaignId1, List.of(scenarioExecutionReportCampaign4), "", false, scenarioExecution2.environment(), scenarioExecution2.datasetId().orElse(null), scenarioExecution2.datasetVersion().orElse(null), scenarioExecution2.user());

                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign5 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution4.testCaseTitle(), scenarioExecution4);
                CampaignExecutionReport campaignExecutionReport5 = new CampaignExecutionReport(3L, campaignId1, List.of(scenarioExecutionReportCampaign5), "", false, scenarioExecution4.environment(), scenarioExecution4.datasetId().orElse(null), scenarioExecution4.datasetVersion().orElse(null), scenarioExecution4.user());

                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign6 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution6.testCaseTitle(), scenarioExecution6);
                CampaignExecutionReport campaignExecutionReport6 = new CampaignExecutionReport(oldestCampaignExecutionId2, campaignId1, List.of(scenarioExecutionReportCampaign6), "", false, scenarioExecution6.environment(), scenarioExecution6.datasetId().orElse(null), scenarioExecution6.datasetVersion().orElse(null), scenarioExecution6.user());

                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                    scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                    scenarioExecution2.withCampaignReport(campaignExecutionReport4),
                    scenarioExecution3.withCampaignReport(campaignExecutionReport2),
                    scenarioExecution4.withCampaignReport(campaignExecutionReport5),
                    scenarioExecution5.withCampaignReport(campaignExecutionReport3),
                    scenarioExecution6.withCampaignReport(campaignExecutionReport6)
                ));

                CampaignRepository campaignRepository = mock(CampaignRepository.class);
                when(campaignRepository.findAll()).thenReturn(List.of(
                    CampaignBuilder.builder().setId(campaignId1).build(),
                    CampaignBuilder.builder().setId(campaignId2).build()
                ));
                CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                when(campaignRepository.findExecutionsById(campaignId1)).thenReturn(List.of(
                    campaignExecutionReport1, campaignExecutionReport2, campaignExecutionReport3
                ));
                when(campaignRepository.findExecutionsById(campaignId2)).thenReturn(List.of(
                    campaignExecutionReport4, campaignExecutionReport5, campaignExecutionReport6
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest campaign's execution is deleted
                // The associated scenario's execution is kept
                verify(testCaseRepository).findAll();
                verify(executionsRepository).getExecutions(scenarioId);
                verify(executionsRepository, times(0)).deleteExecutions(anySet());
                verify(campaignRepository).findAll();
                verify(campaignRepository).findExecutionsById(campaignId1);
                verify(campaignRepository).findExecutionsById(campaignId2);
                verify(campaignExecutionRepository).deleteExecutions(Set.of(oldestCampaignExecutionId1));
                verify(campaignExecutionRepository).deleteExecutions(Set.of(oldestCampaignExecutionId2));
                assertThat(report.scenariosExecutionsIds()).isEmpty();
                assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(oldestCampaignExecutionId1, oldestCampaignExecutionId2);
            }
        }
    }

    @Nested
    @DisplayName("keeps last success no matter what")
    class KeepsLastSuccess {
        @Nested
        @DisplayName("for scenarios' executions independents of any campaigns' ones")
        class Scenarios {
            @Test
            void keeps_last_success() {
                // Given
                // Four scenario's executions independents of any campaigns' execution
                // The last twos are in failure
                // And a configuration limit set to 1 for scenarios' executions
                Integer maxScenarioExecutionsConfiguration = 1;
                String scenarioId = "1";
                Long lastSuccessScenarioExecutionId = 2L;

                TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                when(testCaseRepository.findAll()).thenReturn(List.of(
                    TestCaseMetadataImpl.builder().withId(scenarioId).build()
                ));
                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                LocalDateTime now = now();
                when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                    executionBuilder().executionId(4L).status(FAILURE).time(now).build(),
                    executionBuilder().executionId(3L).status(FAILURE).time(now.minusSeconds(10)).build(),
                    executionBuilder().executionId(lastSuccessScenarioExecutionId).time(now.minusSeconds(20)).build(),
                    executionBuilder().executionId(1L).time(now.minusSeconds(30)).build()
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest scenario's execution is deleted
                verify(testCaseRepository).findAll();
                verify(executionsRepository).getExecutions(scenarioId);
                verify(executionsRepository).deleteExecutions(Set.of(3L, 1L));
                assertThat(report.scenariosExecutionsIds()).containsExactlyInAnyOrder(3L, 1L);
            }
        }

        @Nested
        @DisplayName("for campaigns' executions without manual scenarios' executions")
        class Campaigns {
            @Test
            void keeps_last_success() {
                // Given
                // Four campaign's executions with only one scenario's execution each
                // The last two are in failure
                // And a configuration limit set to 10 for scenarios' executions
                // And a configuration limit set to 1 for campaigns' executions
                Integer maxScenarioExecutionsConfiguration = 10;
                Integer maxCampaignExecutionsConfiguration = 1;
                String scenarioId = "1";
                Long campaignId = 1L;
                Long lastSuccessCampaignExecutionId = 2L;
                LocalDateTime now = now();

                TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
                when(testCaseRepository.findAll()).thenReturn(List.of(
                    TestCaseMetadataImpl.builder().withId(scenarioId).build()
                ));

                ExecutionSummary scenarioExecution1 = executionBuilder().executionId(4L).status(FAILURE).time(now).build();
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
                CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(4L, campaignId, List.of(scenarioExecutionReportCampaign1), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

                ExecutionSummary scenarioExecution2 = executionBuilder().executionId(3L).status(FAILURE).time(now.minusSeconds(10)).build();
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
                CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(3L, campaignId, List.of(scenarioExecutionReportCampaign2), "", false, scenarioExecution2.environment(), scenarioExecution2.datasetId().orElse(null), scenarioExecution2.datasetVersion().orElse(null), scenarioExecution2.user());

                ExecutionSummary scenarioExecution3 = executionBuilder().executionId(2L).time(now.minusSeconds(20)).build();
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign3 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution3.testCaseTitle(), scenarioExecution3);
                CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(lastSuccessCampaignExecutionId, campaignId, List.of(scenarioExecutionReportCampaign3), "", false, scenarioExecution3.environment(), scenarioExecution3.datasetId().orElse(null), scenarioExecution3.datasetVersion().orElse(null), scenarioExecution3.user());

                ExecutionSummary scenarioExecution4 = executionBuilder().executionId(1L).time(now.minusSeconds(30)).build();
                ScenarioExecutionReportCampaign scenarioExecutionReportCampaign4 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution4.testCaseTitle(), scenarioExecution4);
                CampaignExecutionReport campaignExecutionReport4 = new CampaignExecutionReport(1L, campaignId, List.of(scenarioExecutionReportCampaign4), "", false, scenarioExecution4.environment(), scenarioExecution4.datasetId().orElse(null), scenarioExecution4.datasetVersion().orElse(null), scenarioExecution4.user());

                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                    scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                    scenarioExecution2.withCampaignReport(campaignExecutionReport2),
                    scenarioExecution3.withCampaignReport(campaignExecutionReport3),
                    scenarioExecution4.withCampaignReport(campaignExecutionReport4)
                ));

                CampaignRepository campaignRepository = mock(CampaignRepository.class);
                when(campaignRepository.findAll()).thenReturn(List.of(
                    CampaignBuilder.builder().setId(campaignId).build()
                ));
                CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                    campaignExecutionReport1, campaignExecutionReport2, campaignExecutionReport3, campaignExecutionReport4
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest campaign's execution is deleted
                // The associated scenario's execution is kept
                verify(testCaseRepository).findAll();
                verify(executionsRepository).getExecutions(scenarioId);
                verify(executionsRepository, times(0)).deleteExecutions(anySet());
                verify(campaignRepository).findAll();
                verify(campaignRepository).findExecutionsById(campaignId);
                verify(campaignExecutionRepository).deleteExecutions(Set.of(3L, 1L));
                assertThat(report.scenariosExecutionsIds()).isEmpty();
                assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(3L, 1L);
            }
        }
    }

    @Nested
    @DisplayName("keeps campaign auto retried scenarios' executions when campaign is not deleted")
    class AutoRetriedScenarioExecutions {
        @Test
        void ignore_auto_retried_scenarios_executions() {
            // Given
            // A campaign with auto retry on with one scenario
            // A campaign's execution with two scenario's executions, first in failure, second in success
            // Two manual scenario's executions older than the others
            // And a configuration limit set to 2 for scenarios' executions
            // And a configuration limit set to 1 for campaigns' executions
            Integer maxScenarioExecutionsConfiguration = 1;
            Integer maxCampaignExecutionsConfiguration = 1;
            String scenarioId = "1";
            Long campaignId = 1L;
            Long oldestScenarioExecutionId = 1L;
            LocalDateTime now = now();

            TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
            when(testCaseRepository.findAll()).thenReturn(List.of(
                TestCaseMetadataImpl.builder().withId(scenarioId).build()
            ));

            ExecutionSummary scenarioExecution1 = executionBuilder().executionId(4L).time(now).build();
            ExecutionSummary scenarioExecution2 = executionBuilder().executionId(3L).time(now.minusSeconds(10)).status(FAILURE).build();
            ExecutionSummary scenarioExecution3 = executionBuilder().executionId(2L).time(now.minusSeconds(20)).build();
            ExecutionSummary scenarioExecution4 = executionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(30)).build();

            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
            CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(1L, campaignId, List.of(scenarioExecutionReportCampaign1, scenarioExecutionReportCampaign2), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

            ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
            when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                scenarioExecution2.withCampaignReport(campaignExecutionReport1),
                scenarioExecution3,
                scenarioExecution4
            ));

            CampaignRepository campaignRepository = mock(CampaignRepository.class);
            when(campaignRepository.findAll()).thenReturn(List.of(
                CampaignBuilder.builder().setId(campaignId).build()
            ));
            CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
            when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                campaignExecutionReport1
            ));

            // When
            PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
            PurgeReport report = sut.purge();

            // Then
            // The oldest scenario's execution is deleted
            // The scenario's execution which was retried is kept
            verify(testCaseRepository).findAll();
            verify(executionsRepository).getExecutions(scenarioId);
            verify(executionsRepository).deleteExecutions(Set.of(oldestScenarioExecutionId));
            verify(campaignRepository).findAll();
            verify(campaignRepository).findExecutionsById(campaignId);
            verify(campaignExecutionRepository, times(0)).deleteExecutions(anySet());
            assertThat(report.scenariosExecutionsIds()).containsExactly(oldestScenarioExecutionId);
            assertThat(report.campaignsExecutionsIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletes campaigns' manual retried executions when older than oldest kept campaign's execution")
    class ManualRetriedCampaignExecutions {
        @Test
        void purge_oldest_campaign_manual_execution() {
            // Given
            // Two campaigns with one scenario
            // First campaign has four executions, with the second and third as partial execution
            // Second campaign has two executions, which frame the first campaign second execution
            // And a configuration limit set to 2 for scenarios' executions
            // And a configuration limit set to 1 for campaigns' executions
            Integer maxScenarioExecutionsConfiguration = 100;
            Integer maxCampaignExecutionsConfiguration = 1;
            String scenarioId = "1";
            Long campaignWithManualReplaysId = 1L;
            Long campaignId = 2L;
            LocalDateTime now = now();

            TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
            when(testCaseRepository.findAll()).thenReturn(List.of(
                TestCaseMetadataImpl.builder().withId(scenarioId).build()
            ));

            // Scenario executions
            ExecutionSummary scenarioExecution1 = executionBuilder().executionId(6L).time(now).build();
            ExecutionSummary scenarioExecution2 = executionBuilder().executionId(5L).time(now).build();
            ExecutionSummary scenarioExecution3 = executionBuilder().executionId(4L).time(now.minusSeconds(10)).build();
            ExecutionSummary scenarioExecution4 = executionBuilder().executionId(3L).time(now.minusSeconds(20)).status(FAILURE).build();
            ExecutionSummary scenarioExecution5 = executionBuilder().executionId(2L).time(now.minusSeconds(30)).build();
            ExecutionSummary scenarioExecution6 = executionBuilder().executionId(1L).time(now.minusSeconds(40)).status(FAILURE).build();

            // First campaign executions
            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign1 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution1.testCaseTitle(), scenarioExecution1);
            CampaignExecutionReport campaignExecutionReport1 = new CampaignExecutionReport(6L, campaignWithManualReplaysId, List.of(scenarioExecutionReportCampaign1), "", false, scenarioExecution1.environment(), scenarioExecution1.datasetId().orElse(null), scenarioExecution1.datasetVersion().orElse(null), scenarioExecution1.user());

            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign2 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution2.testCaseTitle(), scenarioExecution2);
            CampaignExecutionReport campaignExecutionReport2 = new CampaignExecutionReport(5L, campaignWithManualReplaysId, List.of(scenarioExecutionReportCampaign2), "", true, scenarioExecution2.environment(), scenarioExecution2.datasetId().orElse(null), scenarioExecution2.datasetVersion().orElse(null), scenarioExecution2.user());

            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign3 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution4.testCaseTitle(), scenarioExecution4);
            CampaignExecutionReport campaignExecutionReport3 = new CampaignExecutionReport(3L, campaignWithManualReplaysId, List.of(scenarioExecutionReportCampaign3), "", true, scenarioExecution4.environment(), scenarioExecution4.datasetId().orElse(null), scenarioExecution4.datasetVersion().orElse(null), scenarioExecution4.user());

            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign4 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution6.testCaseTitle(), scenarioExecution6);
            CampaignExecutionReport campaignExecutionReport4 = new CampaignExecutionReport(1L, campaignWithManualReplaysId, List.of(scenarioExecutionReportCampaign4), "", false, scenarioExecution6.environment(), scenarioExecution6.datasetId().orElse(null), scenarioExecution6.datasetVersion().orElse(null), scenarioExecution6.user());

            // Second campaign executions
            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign5 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution3.testCaseTitle(), scenarioExecution3);
            CampaignExecutionReport campaignExecutionReport5 = new CampaignExecutionReport(4L, campaignId, List.of(scenarioExecutionReportCampaign5), "", false, scenarioExecution3.environment(), scenarioExecution3.datasetId().orElse(null), scenarioExecution3.datasetVersion().orElse(null), scenarioExecution3.user());

            ScenarioExecutionReportCampaign scenarioExecutionReportCampaign6 = new ScenarioExecutionReportCampaign(scenarioId, scenarioExecution5.testCaseTitle(), scenarioExecution5);
            CampaignExecutionReport campaignExecutionReport6 = new CampaignExecutionReport(2L, campaignId, List.of(scenarioExecutionReportCampaign6), "", false, scenarioExecution5.environment(), scenarioExecution5.datasetId().orElse(null), scenarioExecution5.datasetVersion().orElse(null), scenarioExecution5.user());


            ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
            when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                scenarioExecution1.withCampaignReport(campaignExecutionReport1),
                scenarioExecution2.withCampaignReport(campaignExecutionReport2),
                scenarioExecution3.withCampaignReport(campaignExecutionReport5),
                scenarioExecution4.withCampaignReport(campaignExecutionReport3),
                scenarioExecution5.withCampaignReport(campaignExecutionReport6),
                scenarioExecution6.withCampaignReport(campaignExecutionReport4)
            ));

            CampaignRepository campaignRepository = mock(CampaignRepository.class);
            when(campaignRepository.findAll()).thenReturn(List.of(
                CampaignBuilder.builder().setId(campaignWithManualReplaysId).build(),
                CampaignBuilder.builder().setId(campaignId).build()
            ));
            CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
            when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                campaignExecutionReport5, campaignExecutionReport6
            ));
            when(campaignRepository.findExecutionsById(campaignWithManualReplaysId)).thenReturn(List.of(
                campaignExecutionReport1, campaignExecutionReport2, campaignExecutionReport3, campaignExecutionReport4
            ));

            // When
            PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
            PurgeReport report = sut.purge();

            // Then
            // The oldest first campaign and second campaign's executions are deleted
            // The first campaign partial execution is deleted and the second is kept
            verify(testCaseRepository).findAll();
            verify(executionsRepository).getExecutions(scenarioId);
            verify(executionsRepository, times(0)).deleteExecutions(anySet());
            verify(campaignRepository).findAll();
            verify(campaignRepository).findExecutionsById(campaignId);
            verify(campaignRepository).findExecutionsById(campaignWithManualReplaysId);
            verify(campaignExecutionRepository).deleteExecutions(Set.of(1L, 3L));
            verify(campaignExecutionRepository).deleteExecutions(Set.of(2L));
            assertThat(report.scenariosExecutionsIds()).isEmpty();
            assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    private static ExecutionSummary.Builder executionBuilder() {
        return ExecutionSummary.builder()
            .executionId(-1L)
            .environment("env")
            .time(now())
            .duration(5L)
            .status(SUCCESS)
            .user("executor")
            .testCaseTitle("");
    }
}

package com.chutneytesting.execution.domain;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.FAILURE;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.PurgeService.PurgeReport;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignBuilder;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
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
                        scenarioExecutionBuilder().executionId(1L).build(),
                        scenarioExecutionBuilder().executionId(2L).build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                    PurgeReport report = sut.purge();

                    // Then
                    // The two scenario's executions are not deleted
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
                        scenarioExecutionBuilder().executionId(3L).time(now).build(),
                        scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(10)).build(),
                        scenarioExecutionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(20)).build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest scenario's execution is deleted
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
                        scenarioExecutionBuilder().executionId(1L).environment("env1").build(),
                        scenarioExecutionBuilder().executionId(2L).environment("env2").build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                    PurgeReport report = sut.purge();

                    // Then
                    // The two scenario's executions are not deleted
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
                        scenarioExecutionBuilder().executionId(3L).time(now).environment("env1").build(),
                        scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(10)).environment("env2").build(),
                        scenarioExecutionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(20)).environment("env1").build()
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest scenario's execution on the environment with two executions is deleted
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
                    scenarioExecutionBuilder().executionId(1L).time(now).build(),
                    scenarioExecutionBuilder().executionId(3L).time(now.minusSeconds(10)).build()
                ));
                when(executionsRepository.getExecutions(scenarioId2)).thenReturn(List.of(
                    scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(20)).build()
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                PurgeReport report = sut.purge();

                // Then
                // The oldest scenario's execution is deleted for the scenario with two executions
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

                    ExecutionSummary scenarioExecution1 = scenarioExecutionBuilder().executionId(3L).time(now).build();
                    CampaignExecution campaignExecution1 = buildCampaignExecution(3L, campaignId, tuple(scenarioId, scenarioExecution1));

                    ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(10)).build();
                    CampaignExecution campaignExecution2 = buildCampaignExecution(2L, campaignId, tuple(scenarioId, scenarioExecution2));

                    ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(20)).build();
                    CampaignExecution campaignExecution3 = buildCampaignExecution(oldestCampaignExecutionId, campaignId, tuple(scenarioId, scenarioExecution3));

                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        scenarioExecution1.withCampaignReport(campaignExecution1),
                        scenarioExecution2.withCampaignReport(campaignExecution2),
                        scenarioExecution3.withCampaignReport(campaignExecution3)
                    ));

                    CampaignRepository campaignRepository = mock(CampaignRepository.class);
                    when(campaignRepository.findAll()).thenReturn(List.of(
                        CampaignBuilder.builder().setId(campaignId).build()
                    ));
                    CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                    when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                        campaignExecution1, campaignExecution2, campaignExecution3
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest campaign's execution is deleted
                    // The associated scenario's execution is kept
                    verify(executionsRepository, times(0)).deleteExecutions(anySet());
                    verify(campaignExecutionRepository).deleteExecutions(anySet());
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

                    ExecutionSummary scenarioExecution1 = scenarioExecutionBuilder().executionId(3L).time(now).environment("env1").build();
                    CampaignExecution campaignExecution1 = buildCampaignExecution(3L, campaignId, tuple(scenarioId, scenarioExecution1));

                    ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(10)).environment("env2").build();
                    CampaignExecution campaignExecution2 = buildCampaignExecution(2L, campaignId, tuple(scenarioId, scenarioExecution2));

                    ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(20)).environment("env1").build();
                    CampaignExecution campaignExecution3 = buildCampaignExecution(oldestCampaignExecutionId, campaignId, tuple(scenarioId, scenarioExecution3));

                    ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                    when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                        scenarioExecution1.withCampaignReport(campaignExecution1),
                        scenarioExecution2.withCampaignReport(campaignExecution2),
                        scenarioExecution3.withCampaignReport(campaignExecution3)
                    ));

                    CampaignRepository campaignRepository = mock(CampaignRepository.class);
                    when(campaignRepository.findAll()).thenReturn(List.of(
                        CampaignBuilder.builder().setId(campaignId).build()
                    ));
                    CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                    when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                        campaignExecution1, campaignExecution2, campaignExecution3
                    ));

                    // When
                    PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                    PurgeReport report = sut.purge();

                    // Then
                    // The oldest campaign's execution on the environment with two executions is deleted
                    // The associated scenario's execution is kept
                    verify(executionsRepository, times(0)).deleteExecutions(anySet());
                    verify(campaignExecutionRepository).deleteExecutions(anySet());
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
                ExecutionSummary scenarioExecution1 = scenarioExecutionBuilder().executionId(6L).time(now).build();
                ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(5L).time(now.minusSeconds(10)).build();
                ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(4L).time(now.minusSeconds(20)).build();
                ExecutionSummary scenarioExecution4 = scenarioExecutionBuilder().executionId(3L).time(now.minusSeconds(30)).build();
                ExecutionSummary scenarioExecution5 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(40)).build();
                ExecutionSummary scenarioExecution6 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(50)).build();

                // First campaign executions
                CampaignExecution campaignExecution1 = buildCampaignExecution(6L, campaignId1, tuple(scenarioId, scenarioExecution1));
                CampaignExecution campaignExecution2 = buildCampaignExecution(4L, campaignId1, tuple(scenarioId, scenarioExecution3));
                CampaignExecution campaignExecution3 = buildCampaignExecution(oldestCampaignExecutionId1, campaignId1, tuple(scenarioId, scenarioExecution5));

                // Second campaign executions
                CampaignExecution campaignExecution4 = buildCampaignExecution(5L, campaignId2, tuple(scenarioId, scenarioExecution2));
                CampaignExecution campaignExecution5 = buildCampaignExecution(3L, campaignId2, tuple(scenarioId, scenarioExecution4));
                CampaignExecution campaignExecution6 = buildCampaignExecution(oldestCampaignExecutionId2, campaignId2, tuple(scenarioId, scenarioExecution6));

                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                    scenarioExecution1.withCampaignReport(campaignExecution1),
                    scenarioExecution2.withCampaignReport(campaignExecution4),
                    scenarioExecution3.withCampaignReport(campaignExecution2),
                    scenarioExecution4.withCampaignReport(campaignExecution5),
                    scenarioExecution5.withCampaignReport(campaignExecution3),
                    scenarioExecution6.withCampaignReport(campaignExecution6)
                ));

                CampaignRepository campaignRepository = mock(CampaignRepository.class);
                when(campaignRepository.findAll()).thenReturn(List.of(
                    CampaignBuilder.builder().setId(campaignId1).build(),
                    CampaignBuilder.builder().setId(campaignId2).build()
                ));
                CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                when(campaignRepository.findExecutionsById(campaignId1)).thenReturn(List.of(
                    campaignExecution1, campaignExecution2, campaignExecution3
                ));
                when(campaignRepository.findExecutionsById(campaignId2)).thenReturn(List.of(
                    campaignExecution4, campaignExecution5, campaignExecution6
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest campaign's execution is deleted
                // The associated scenario's execution is kept
                verify(executionsRepository, times(0)).deleteExecutions(anySet());
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
                    scenarioExecutionBuilder().executionId(4L).status(FAILURE).time(now).build(),
                    scenarioExecutionBuilder().executionId(3L).status(FAILURE).time(now.minusSeconds(10)).build(),
                    scenarioExecutionBuilder().executionId(lastSuccessScenarioExecutionId).time(now.minusSeconds(20)).build(),
                    scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(30)).build()
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, mock(CampaignRepository.class), mock(CampaignExecutionRepository.class), maxScenarioExecutionsConfiguration, 100);
                PurgeReport report = sut.purge();

                // Then
                // The oldest scenario's execution is deleted
                verify(executionsRepository).deleteExecutions(anySet());
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

                ExecutionSummary scenarioExecution1 = scenarioExecutionBuilder().executionId(4L).status(FAILURE).time(now).build();
                CampaignExecution campaignExecution1 = buildCampaignExecution(4L, campaignId, tuple(scenarioId, scenarioExecution1));

                ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(3L).status(FAILURE).time(now.minusSeconds(10)).build();
                CampaignExecution campaignExecution2 = buildCampaignExecution(3L, campaignId, tuple(scenarioId, scenarioExecution2));

                ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(20)).build();
                CampaignExecution campaignExecution3 = buildCampaignExecution(lastSuccessCampaignExecutionId, campaignId, tuple(scenarioId, scenarioExecution3));

                ExecutionSummary scenarioExecution4 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(30)).build();
                CampaignExecution campaignExecution4 = buildCampaignExecution(1L, campaignId, tuple(scenarioId, scenarioExecution4));

                ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
                when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                    scenarioExecution1.withCampaignReport(campaignExecution1),
                    scenarioExecution2.withCampaignReport(campaignExecution2),
                    scenarioExecution3.withCampaignReport(campaignExecution3),
                    scenarioExecution4.withCampaignReport(campaignExecution4)
                ));

                CampaignRepository campaignRepository = mock(CampaignRepository.class);
                when(campaignRepository.findAll()).thenReturn(List.of(
                    CampaignBuilder.builder().setId(campaignId).build()
                ));
                CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
                when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                    campaignExecution1, campaignExecution2, campaignExecution3, campaignExecution4
                ));

                // When
                PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
                PurgeReport report = sut.purge();

                // Then
                // The oldest campaign's execution is deleted
                // The associated scenario's execution is kept
                verify(executionsRepository, times(0)).deleteExecutions(anySet());
                verify(campaignExecutionRepository).deleteExecutions(anySet());
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
            // And a configuration limit set to 1 for scenarios' executions
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

            ExecutionSummary autoRetryScenarioExecution2 = scenarioExecutionBuilder().executionId(4L).time(now).build();
            ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(3L).time(now.minusSeconds(10)).status(FAILURE).build();
            ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(20)).build();
            ExecutionSummary scenarioExecution4 = scenarioExecutionBuilder().executionId(oldestScenarioExecutionId).time(now.minusSeconds(30)).build();

            CampaignExecution campaignExecution1 = buildCampaignExecution(1L, campaignId, tuple(scenarioId, autoRetryScenarioExecution2), tuple(scenarioId, scenarioExecution2));

            ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
            when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                autoRetryScenarioExecution2.withCampaignReport(campaignExecution1),
                scenarioExecution2.withCampaignReport(campaignExecution1),
                scenarioExecution3,
                scenarioExecution4
            ));

            CampaignRepository campaignRepository = mock(CampaignRepository.class);
            when(campaignRepository.findAll()).thenReturn(List.of(
                CampaignBuilder.builder().setId(campaignId).build()
            ));
            CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
            when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                campaignExecution1
            ));

            // When
            PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
            PurgeReport report = sut.purge();

            // Then
            // The oldest scenario's execution is deleted
            // The scenario's execution which was retried is kept
            verify(executionsRepository).deleteExecutions(anySet());
            verify(executionsRepository).deleteExecutions(Set.of(oldestScenarioExecutionId));
            verify(campaignExecutionRepository, times(0)).deleteExecutions(anySet());
            assertThat(report.scenariosExecutionsIds()).containsExactly(oldestScenarioExecutionId);
            assertThat(report.campaignsExecutionsIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("deletes campaigns' manual retried (i.e. potentially partial) executions when older than oldest kept campaign's execution")
    class ManualRetriedCampaignExecutions {
        @Test
        void purge_oldest_campaign_manual_execution() {
            // Given
            // Two campaigns with one scenario
            // First campaign has four executions, with the second and third as partial execution
            // Second campaign has two executions, which frame the first campaign second execution
            // And a configuration limit set to 100 for scenarios' executions
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
            ExecutionSummary scenarioExecution1 = scenarioExecutionBuilder().executionId(6L).time(now).build();
            ExecutionSummary scenarioExecution2 = scenarioExecutionBuilder().executionId(5L).time(now).build();
            ExecutionSummary scenarioExecution3 = scenarioExecutionBuilder().executionId(4L).time(now.minusSeconds(10)).build();
            ExecutionSummary scenarioExecution4 = scenarioExecutionBuilder().executionId(3L).time(now.minusSeconds(20)).status(FAILURE).build();
            ExecutionSummary scenarioExecution5 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(30)).build();
            ExecutionSummary scenarioExecution6 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(40)).status(FAILURE).build();

            // First campaign executions
            CampaignExecution campaignExecution1 = buildCampaignExecution(6L, campaignWithManualReplaysId, tuple(scenarioId, scenarioExecution1));
            CampaignExecution campaignExecution2 = buildCampaignExecution(5L, campaignWithManualReplaysId, true, tuple(scenarioId, scenarioExecution2));
            CampaignExecution campaignExecution3 = buildCampaignExecution(3L, campaignWithManualReplaysId, true, tuple(scenarioId, scenarioExecution4));
            CampaignExecution campaignExecution4 = buildCampaignExecution(1L, campaignWithManualReplaysId, tuple(scenarioId, scenarioExecution6));

            // Second campaign executions
            CampaignExecution campaignExecution5 = buildCampaignExecution(4L, campaignId, tuple(scenarioId, scenarioExecution3));
            CampaignExecution campaignExecution6 = buildCampaignExecution(2L, campaignId, tuple(scenarioId, scenarioExecution5));

            ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
            when(executionsRepository.getExecutions(scenarioId)).thenReturn(List.of(
                scenarioExecution1.withCampaignReport(campaignExecution1),
                scenarioExecution2.withCampaignReport(campaignExecution2),
                scenarioExecution3.withCampaignReport(campaignExecution5),
                scenarioExecution4.withCampaignReport(campaignExecution3),
                scenarioExecution5.withCampaignReport(campaignExecution6),
                scenarioExecution6.withCampaignReport(campaignExecution4)
            ));

            CampaignRepository campaignRepository = mock(CampaignRepository.class);
            when(campaignRepository.findAll()).thenReturn(List.of(
                CampaignBuilder.builder().setId(campaignWithManualReplaysId).build(),
                CampaignBuilder.builder().setId(campaignId).build()
            ));
            CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
            when(campaignRepository.findExecutionsById(campaignId)).thenReturn(List.of(
                campaignExecution5, campaignExecution6
            ));
            when(campaignRepository.findExecutionsById(campaignWithManualReplaysId)).thenReturn(List.of(
                campaignExecution1, campaignExecution2, campaignExecution3, campaignExecution4
            ));

            // When
            PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
            PurgeReport report = sut.purge();

            // Then
            // The oldest first campaign and second campaign's executions are deleted
            // The first campaign partial execution is deleted and the second is kept
            verify(executionsRepository, times(0)).deleteExecutions(anySet());
            verify(campaignExecutionRepository, times(2)).deleteExecutions(anySet());
            verify(campaignExecutionRepository).deleteExecutions(Set.of(1L, 3L));
            verify(campaignExecutionRepository).deleteExecutions(Set.of(2L));
            assertThat(report.scenariosExecutionsIds()).isEmpty();
            assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        }
    }

    @Nested
    @DisplayName("purge executions")
    class MixRulesPurge {
        @Test
        void purge_executions() {
            // Given
            Integer maxScenarioExecutionsConfiguration = 5;
            Integer maxCampaignExecutionsConfiguration = 3;
            String scenarioId1 = "1";
            String scenarioId2 = "2";
            String env1 = "dev";
            String env2 = "stg";
            String env3 = "qa";
            Long campaignId1 = 1L;
            Long campaignId2 = 2L;
            LocalDateTime now = now();

            // Two scenarios
            TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
            when(testCaseRepository.findAll()).thenReturn(List.of(
                TestCaseMetadataImpl.builder().withId(scenarioId1).build(),
                TestCaseMetadataImpl.builder().withId(scenarioId2).build()
            ));
            // Two campaigns
            CampaignRepository campaignRepository = mock(CampaignRepository.class);
            when(campaignRepository.findAll()).thenReturn(List.of(
                CampaignBuilder.builder().setId(campaignId1).build(),
                CampaignBuilder.builder().setId(campaignId2).build()
            ));

            // First scenario executions
            ExecutionSummary se1_ce1 = scenarioExecutionBuilder().executionId(29L).time(now).environment(env1).build();
            ExecutionSummary se2_ce10 = scenarioExecutionBuilder().executionId(28L).time(now.minusSeconds(10)).environment(env1).build();
            ExecutionSummary se3_ce5 = scenarioExecutionBuilder().executionId(27L).time(now.minusSeconds(20)).environment(env2).status(FAILURE).build();
            ExecutionSummary se4_ce6 = scenarioExecutionBuilder().executionId(26L).time(now.minusSeconds(30)).environment(env2).build();
            ExecutionSummary se5 = scenarioExecutionBuilder().executionId(25L).time(now.minusSeconds(30)).environment(env3).build();
            ExecutionSummary se6_ce2 = scenarioExecutionBuilder().executionId(24L).time(now.minusSeconds(40)).environment(env1).build();
            ExecutionSummary se7_ce2 = scenarioExecutionBuilder().executionId(23L).time(now.minusSeconds(50)).environment(env1).status(FAILURE).build();
            ExecutionSummary se8_ce11 = scenarioExecutionBuilder().executionId(22L).time(now.minusSeconds(60)).environment(env1).build();
            ExecutionSummary se9_ce15 = scenarioExecutionBuilder().executionId(21L).time(now.minusSeconds(70)).environment(env2).status(FAILURE).build();
            ExecutionSummary se10_ce7 = scenarioExecutionBuilder().executionId(20L).time(now.minusSeconds(80)).environment(env2).build();
            ExecutionSummary se11_ce16 = scenarioExecutionBuilder().executionId(19L).time(now.minusSeconds(90)).environment(env2).build();
            ExecutionSummary se12 = scenarioExecutionBuilder().executionId(18L).time(now.minusSeconds(100)).environment(env2).status(FAILURE).build();
            ExecutionSummary se13 = scenarioExecutionBuilder().executionId(17L).time(now.minusSeconds(110)).environment(env2).build();
            ExecutionSummary se14 = scenarioExecutionBuilder().executionId(16L).time(now.minusSeconds(120)).environment(env2).build();
            ExecutionSummary se15 = scenarioExecutionBuilder().executionId(15L).time(now.minusSeconds(120)).environment(env3).build();
            ExecutionSummary se16_ce12 = scenarioExecutionBuilder().executionId(14L).time(now.minusSeconds(130)).environment(env1).build();
            ExecutionSummary se17_ce13 = scenarioExecutionBuilder().executionId(13L).time(now.minusSeconds(140)).environment(env1).status(FAILURE).build();
            ExecutionSummary se18_ce3 = scenarioExecutionBuilder().executionId(12L).time(now.minusSeconds(150)).environment(env1).build();
            ExecutionSummary se19_ce17 = scenarioExecutionBuilder().executionId(11L).time(now.minusSeconds(160)).environment(env2).status(FAILURE).build();
            ExecutionSummary se20 = scenarioExecutionBuilder().executionId(10L).time(now.minusSeconds(170)).environment(env1).status(FAILURE).build();
            ExecutionSummary se21 = scenarioExecutionBuilder().executionId(9L).time(now.minusSeconds(180)).environment(env2).build();
            ExecutionSummary se22 = scenarioExecutionBuilder().executionId(8L).time(now.minusSeconds(190)).environment(env3).build();
            ExecutionSummary se23_ce18 = scenarioExecutionBuilder().executionId(7L).time(now.minusSeconds(190)).environment(env2).build();
            ExecutionSummary se24 = scenarioExecutionBuilder().executionId(6L).time(now.minusSeconds(200)).environment(env2).status(FAILURE).build();
            ExecutionSummary se25 = scenarioExecutionBuilder().executionId(5L).time(now.minusSeconds(210)).environment(env1).status(FAILURE).build();
            ExecutionSummary se26_ce14 = scenarioExecutionBuilder().executionId(4L).time(now.minusSeconds(220)).environment(env1).build();
            ExecutionSummary se27 = scenarioExecutionBuilder().executionId(3L).time(now.minusSeconds(230)).environment(env1).status(FAILURE).build();
            ExecutionSummary se28_ce9 = scenarioExecutionBuilder().executionId(2L).time(now.minusSeconds(240)).environment(env2).build();
            ExecutionSummary se29_ce4 = scenarioExecutionBuilder().executionId(1L).time(now.minusSeconds(250)).environment(env1).build();

            // Second scenario executions
            ExecutionSummary se30_ce1 = scenarioExecutionBuilder().executionId(61L).time(now.minusSeconds(5)).environment(env1).build();
            ExecutionSummary se31_ce10 = scenarioExecutionBuilder().executionId(60L).time(now.minusSeconds(15)).environment(env1).build();
            ExecutionSummary se32_ce5 = scenarioExecutionBuilder().executionId(59L).time(now.minusSeconds(25)).environment(env2).status(FAILURE).build();
            ExecutionSummary se33_ce6 = scenarioExecutionBuilder().executionId(58L).time(now.minusSeconds(35)).environment(env2).build();
            ExecutionSummary se34 = scenarioExecutionBuilder().executionId(57L).time(now.minusSeconds(45)).environment(env3).build();
            ExecutionSummary se35_ce11 = scenarioExecutionBuilder().executionId(56L).time(now.minusSeconds(45)).environment(env1).build();
            ExecutionSummary se36_ce2 = scenarioExecutionBuilder().executionId(55L).time(now.minusSeconds(55)).environment(env1).build();
            ExecutionSummary se37_ce13 = scenarioExecutionBuilder().executionId(54L).time(now.minusSeconds(65)).environment(env1).build();
            ExecutionSummary se38_ce7 = scenarioExecutionBuilder().executionId(53L).time(now.minusSeconds(75)).environment(env2).status(FAILURE).build();
            ExecutionSummary se39_ce15 = scenarioExecutionBuilder().executionId(52L).time(now.minusSeconds(85)).environment(env2).build();
            ExecutionSummary se40 = scenarioExecutionBuilder().executionId(51L).time(now.minusSeconds(95)).environment(env3).build();
            ExecutionSummary se41 = scenarioExecutionBuilder().executionId(50L).time(now.minusSeconds(95)).environment(env3).build();
            ExecutionSummary se42 = scenarioExecutionBuilder().executionId(49L).time(now.minusSeconds(95)).environment(env2).build();
            ExecutionSummary se43_ce16 = scenarioExecutionBuilder().executionId(48L).time(now.minusSeconds(105)).environment(env2).status(FAILURE).build();
            ExecutionSummary se44 = scenarioExecutionBuilder().executionId(47L).time(now.minusSeconds(115)).environment(env2).build();
            ExecutionSummary se45 = scenarioExecutionBuilder().executionId(46L).time(now.minusSeconds(125)).environment(env3).build();
            ExecutionSummary se46 = scenarioExecutionBuilder().executionId(45L).time(now.minusSeconds(125)).environment(env2).build();
            ExecutionSummary se47 = scenarioExecutionBuilder().executionId(44L).time(now.minusSeconds(135)).environment(env1).build();
            ExecutionSummary se48 = scenarioExecutionBuilder().executionId(43L).time(now.minusSeconds(145)).environment(env1).status(FAILURE).build();
            ExecutionSummary se49 = scenarioExecutionBuilder().executionId(42L).time(now.minusSeconds(155)).environment(env1).build();
            ExecutionSummary se50_ce3 = scenarioExecutionBuilder().executionId(41L).time(now.minusSeconds(165)).environment(env2).status(FAILURE).build();
            ExecutionSummary se51 = scenarioExecutionBuilder().executionId(40L).time(now.minusSeconds(175)).environment(env1).status(FAILURE).build();
            ExecutionSummary se52_ce17 = scenarioExecutionBuilder().executionId(39L).time(now.minusSeconds(185)).environment(env2).status(FAILURE).build();
            ExecutionSummary se53 = scenarioExecutionBuilder().executionId(38L).time(now.minusSeconds(195)).environment(env3).build();
            ExecutionSummary se54_ce8 = scenarioExecutionBuilder().executionId(37L).time(now.minusSeconds(195)).environment(env2).build();
            ExecutionSummary se55_ce9 = scenarioExecutionBuilder().executionId(36L).time(now.minusSeconds(205)).environment(env2).status(FAILURE).build();
            ExecutionSummary se56 = scenarioExecutionBuilder().executionId(35L).time(now.minusSeconds(215)).environment(env1).status(FAILURE).build();
            ExecutionSummary se57_ce14 = scenarioExecutionBuilder().executionId(34L).time(now.minusSeconds(225)).environment(env1).build();
            ExecutionSummary se58_ce14 = scenarioExecutionBuilder().executionId(33L).time(now.minusSeconds(235)).environment(env1).status(FAILURE).build();
            ExecutionSummary se59_ce18 = scenarioExecutionBuilder().executionId(32L).time(now.minusSeconds(245)).environment(env2).build();
            ExecutionSummary se60 = scenarioExecutionBuilder().executionId(31L).time(now.minusSeconds(255)).environment(env3).build();
            ExecutionSummary se61_ce4 = scenarioExecutionBuilder().executionId(30L).time(now.minusSeconds(265)).environment(env1).build();

            // First campaign executions - env1
            CampaignExecution ce1 = buildCampaignExecution(18L, campaignId1, tuple(scenarioId1, se1_ce1), tuple(scenarioId2, se30_ce1));
            CampaignExecution ce2 = buildCampaignExecution(17L, campaignId1, tuple(scenarioId1, se6_ce2), tuple(scenarioId1, se7_ce2), tuple(scenarioId2, se36_ce2)); // auto-retry
            CampaignExecution ce3 = buildCampaignExecution(16L, campaignId1, tuple(scenarioId1, se18_ce3), tuple(scenarioId2, se50_ce3));
            CampaignExecution ce4 = buildCampaignExecution(15L, campaignId1, tuple(scenarioId1, se29_ce4), tuple(scenarioId2, se61_ce4));
            // First campaign executions - env2
            CampaignExecution ce5 = buildCampaignExecution(14L, campaignId1, tuple(scenarioId1, se3_ce5), tuple(scenarioId2, se32_ce5));
            CampaignExecution ce6 = buildCampaignExecution(13L, campaignId1, tuple(scenarioId1, se4_ce6), tuple(scenarioId2, se33_ce6));
            CampaignExecution ce7 = buildCampaignExecution(12L, campaignId1, tuple(scenarioId1, se10_ce7), tuple(scenarioId2, se38_ce7));
            CampaignExecution ce8 = buildCampaignExecution(11L, campaignId1, true, tuple(scenarioId2, se54_ce8)); // manual-retry (partial execution) of ce9
            CampaignExecution ce9 = buildCampaignExecution(10L, campaignId1, tuple(scenarioId1, se28_ce9), tuple(scenarioId2, se55_ce9));

            // Second campaign executions - env1
            CampaignExecution ce10 = buildCampaignExecution(9L, campaignId2, tuple(scenarioId1, se2_ce10), tuple(scenarioId2, se31_ce10));
            CampaignExecution ce11 = buildCampaignExecution(8L, campaignId2, tuple(scenarioId1, se8_ce11), tuple(scenarioId2, se35_ce11));
            CampaignExecution ce12 = buildCampaignExecution(7L, campaignId2, true, tuple(scenarioId1, se16_ce12)); // manual-retry (partial execution) of ce13
            CampaignExecution ce13 = buildCampaignExecution(6L, campaignId2, tuple(scenarioId1, se17_ce13), tuple(scenarioId2, se37_ce13));
            CampaignExecution ce14 = buildCampaignExecution(5L, campaignId2, tuple(scenarioId1, se26_ce14), tuple(scenarioId2, se57_ce14), tuple(scenarioId2, se58_ce14)); // auto-retry
            // Second campaign executions - env2 (all failures except last one)
            CampaignExecution ce15 = buildCampaignExecution(4L, campaignId2, tuple(scenarioId1, se9_ce15), tuple(scenarioId2, se39_ce15));
            CampaignExecution ce16 = buildCampaignExecution(3L, campaignId2, tuple(scenarioId1, se11_ce16), tuple(scenarioId2, se43_ce16));
            CampaignExecution ce17 = buildCampaignExecution(2L, campaignId2, tuple(scenarioId1, se19_ce17), tuple(scenarioId2, se52_ce17));
            CampaignExecution ce18 = buildCampaignExecution(1L, campaignId2, tuple(scenarioId1, se23_ce18), tuple(scenarioId2, se59_ce18));

            // Scenarios executions stub
            ExecutionHistoryRepository executionsRepository = mock(ExecutionHistoryRepository.class);
            when(executionsRepository.getExecutions(scenarioId1)).thenReturn(List.of(
                se1_ce1.withCampaignReport(ce1),
                se2_ce10.withCampaignReport(ce10),
                se3_ce5.withCampaignReport(ce5),
                se4_ce6.withCampaignReport(ce6),
                se5,
                se6_ce2.withCampaignReport(ce2),
                se7_ce2.withCampaignReport(ce2),
                se8_ce11.withCampaignReport(ce11),
                se9_ce15.withCampaignReport(ce15),
                se10_ce7.withCampaignReport(ce7),
                se11_ce16.withCampaignReport(ce16),
                se12, se13, se14, se15,
                se16_ce12.withCampaignReport(ce12),
                se17_ce13.withCampaignReport(ce13),
                se18_ce3.withCampaignReport(ce3),
                se19_ce17.withCampaignReport(ce17),
                se20, se21, se22,
                se23_ce18.withCampaignReport(ce18),
                se24, se25,
                se26_ce14, // se26_ce14.withCampaignReport(ce14), after campaign deletion
                se27,
                se28_ce9, // se28_ce9.withCampaignReport(ce9), after campaign deletion
                se29_ce4 // se29_ce4.withCampaignReport(ce4) after campaign deletion
            ));
            when(executionsRepository.getExecutions(scenarioId2)).thenReturn(List.of(
                se30_ce1.withCampaignReport(ce1),
                se31_ce10.withCampaignReport(ce10),
                se32_ce5.withCampaignReport(ce5),
                se33_ce6.withCampaignReport(ce6),
                se34,
                se35_ce11.withCampaignReport(ce11),
                se36_ce2.withCampaignReport(ce2),
                se37_ce13.withCampaignReport(ce13),
                se38_ce7.withCampaignReport(ce7),
                se39_ce15.withCampaignReport(ce15),
                se40, se41, se42,
                se43_ce16.withCampaignReport(ce16),
                se44, se45, se46, se47, se48, se49,
                se50_ce3.withCampaignReport(ce3),
                se51,
                se52_ce17.withCampaignReport(ce17),
                se53,
                se54_ce8, // se54_ce8.withCampaignReport(ce8), after campaign deletion
                se55_ce9, // se55_ce9.withCampaignReport(ce9), after campaign deletion
                se56,
                se57_ce14, // se57_ce14.withCampaignReport(ce14), after campaign deletion
                se58_ce14, // se58_ce14.withCampaignReport(ce14), after campaign deletion
                se59_ce18.withCampaignReport(ce18),
                se60,
                se61_ce4 // se61_ce4.withCampaignReport(ce4) after campaign deletion
            ));

            // Campaigns executions stub
            CampaignExecutionRepository campaignExecutionRepository = mock(CampaignExecutionRepository.class);
            when(campaignRepository.findExecutionsById(campaignId1)).thenReturn(List.of(
                ce1, ce2, ce3, ce4, ce5, ce6, ce7, ce8, ce9
            ));
            when(campaignRepository.findExecutionsById(campaignId2)).thenReturn(List.of(
                ce10, ce11, ce12, ce13, ce14, ce15, ce16, ce17, ce18
            ));

            // When
            PurgeServiceImpl sut = new PurgeServiceImpl(testCaseRepository, executionsRepository, campaignRepository, campaignExecutionRepository, maxScenarioExecutionsConfiguration, maxCampaignExecutionsConfiguration);
            PurgeReport report = sut.purge();

            // Then
            verify(campaignExecutionRepository, times(3)).deleteExecutions(anySet());
            verify(campaignExecutionRepository).deleteExecutions(Set.of(15L)); // first campaign env1
            verify(campaignExecutionRepository).deleteExecutions(Set.of(10L, 11L)); // first campaign env2
            verify(campaignExecutionRepository).deleteExecutions(Set.of(5L)); // second campaign env1
            assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(5L, 10L, 11L, 15L);

            verify(executionsRepository, times(3)).deleteExecutions(anySet());
            verify(executionsRepository).deleteExecutions(Set.of(31L)); // second scenario env3
            verify(executionsRepository).deleteExecutions(Set.of(30L, 33L, 34L)); // second scenario env1
            verify(executionsRepository).deleteExecutions(Set.of(2L)); // first scenario env2
            assertThat(report.scenariosExecutionsIds()).containsExactlyInAnyOrder(2L, 30L, 31L, 33L, 34L);
        }
    }

    private static ExecutionSummary.Builder scenarioExecutionBuilder() {
        return ExecutionSummary.builder()
            .executionId(-1L)
            .environment("env")
            .time(now())
            .duration(5L)
            .status(SUCCESS)
            .user("executor")
            .testCaseTitle("");
    }

    /**
     * scenarioExecutions tuples are composed of (scenario id as String, scenario execution as ExecutionSummary)
     */
    private static CampaignExecution buildCampaignExecution(Long campaignExecutionId, Long campaignId, boolean partialExecution, Tuple... scenarioExecutions) {
        List<ScenarioExecutionCampaign> scenarioExecutionsForCampaign = Arrays.stream(scenarioExecutions)
            .map(t -> {
                List<Object> tt = t.toList();
                String scenarioId = (String) tt.get(0);
                ExecutionHistory.ExecutionSummary scenarioExecution = (ExecutionHistory.ExecutionSummary) tt.get(1);
                return new ScenarioExecutionCampaign(scenarioId, scenarioExecution.testCaseTitle(), scenarioExecution);
            })
            .toList();

        ExecutionHistory.ExecutionSummary firsScenarioExecution = scenarioExecutionsForCampaign.get(0).execution;
        return new CampaignExecution(
            campaignExecutionId,
            campaignId,
            scenarioExecutionsForCampaign,
            "",
            partialExecution,
            firsScenarioExecution.environment(),
            firsScenarioExecution.datasetId().orElse(null),
            firsScenarioExecution.datasetVersion().orElse(null),
            firsScenarioExecution.user()
        );
    }

    private static CampaignExecution buildCampaignExecution(Long campaignExecutionId, Long campaignId, Tuple... scenarioExecutions) {
        return buildCampaignExecution(campaignExecutionId, campaignId, false, scenarioExecutions);
    }
}

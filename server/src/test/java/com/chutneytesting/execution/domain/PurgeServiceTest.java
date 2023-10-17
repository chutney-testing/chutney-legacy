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
            // And a configuration limit set to 1 for scenarios' executions
            // And a configuration limit set to 100 for campaigns' executions
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
            verify(campaignExecutionRepository).deleteExecutions(Set.of(1L, 3L));
            verify(campaignExecutionRepository).deleteExecutions(Set.of(2L));
            assertThat(report.scenariosExecutionsIds()).isEmpty();
            assertThat(report.campaignsExecutionsIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
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

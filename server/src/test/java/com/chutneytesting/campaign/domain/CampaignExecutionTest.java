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

package com.chutneytesting.campaign.domain;

import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.FAILURE;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.NOT_EXECUTED;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.RUNNING;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.STOPPED;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReportBuilder;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class CampaignExecutionTest {

    @Test
    public void should_take_the_earliest_scenario_start_date_as_start_date() {
        // Given
        ScenarioExecutionCampaign execution_noTime = new ScenarioExecutionCampaign("1", "...", mock(ExecutionHistory.ExecutionSummary.class));

        ExecutionHistory.ExecutionSummary execution_5mn = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution_5mn.time()).thenReturn(LocalDateTime.now().minusMinutes(5));
        ScenarioExecutionCampaign scenarioReport_5mn = new ScenarioExecutionCampaign("2", "...", execution_5mn);

        ExecutionHistory.ExecutionSummary execution_2mn = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution_2mn.time()).thenReturn(LocalDateTime.now().minusMinutes(2));
        ScenarioExecutionCampaign scenarioReport_2mn = new ScenarioExecutionCampaign("3", "...", execution_2mn);

        // When
        CampaignExecution campaignReport = new CampaignExecution(1L, 1L, Lists.list(execution_noTime, scenarioReport_5mn, scenarioReport_2mn), "...", false, "", null, null, "");

        // Then
        assertThat(campaignReport.startDate).isEqualTo(execution_5mn.time());
    }

    @Test
    public void should_set_start_date_as_min_possible_date_when_no_scenario_times_are_available() {
        // Given
        ScenarioExecutionCampaign scenarioReport1 = new ScenarioExecutionCampaign("1", "...", mock(ExecutionHistory.ExecutionSummary.class));
        ScenarioExecutionCampaign scenarioReport2 = new ScenarioExecutionCampaign("2", "...", mock(ExecutionHistory.ExecutionSummary.class));

        // When
        CampaignExecution campaignReport = new CampaignExecution(1L, 1L, Lists.list(scenarioReport1, scenarioReport2), "...", false, "", null, null, "");

        // Then
        assertThat(campaignReport.startDate).isEqualTo(LocalDateTime.MIN);
    }

    @Test
    public void should_start_campaign_execution_when_instantiate_with_no_scenario_reports() {
        // When
        LocalDateTime beforeInstanciation = LocalDateTime.now().minusSeconds(1);
        CampaignExecution campaignReport = new CampaignExecution(1L, "...", false, "", null, null, "");
        // Then
        assertThat(campaignReport.startDate).isAfter(beforeInstanciation);
        assertThat(campaignReport.status()).isEqualTo(RUNNING);
    }

    @Test
    public void should_set_status_when_instantiate_with_empty_scenario_reports() {
        // When
        CampaignExecution campaignReport = new CampaignExecution(1L, 1L, emptyList(), "...", false, "", null, null, "");
        // Then
        assertThat(campaignReport.status()).isEqualTo(SUCCESS);
    }

    @Test
    public void should_set_to_worst_status_when_instantiate_with_scenario_reports() {
        // Given
        ScenarioExecutionCampaign execution_noStatus = new ScenarioExecutionCampaign("1", "...", mock(ExecutionHistory.ExecutionSummary.class));

        ExecutionHistory.ExecutionSummary execution_SUCESS = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution_SUCESS.status()).thenReturn(SUCCESS);
        ScenarioExecutionCampaign scenarioReport_SUCCESS = new ScenarioExecutionCampaign("2", "...", execution_SUCESS);

        ExecutionHistory.ExecutionSummary execution_FAILURE = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution_FAILURE.status()).thenReturn(FAILURE);
        ScenarioExecutionCampaign scenarioReport_FAILURE = new ScenarioExecutionCampaign("3", "...", execution_FAILURE);
        // When
        CampaignExecution campaignReport = new CampaignExecution(1L, 1L, Lists.list(execution_noStatus, scenarioReport_SUCCESS, scenarioReport_FAILURE), "...", false, "", null, null, "");
        // Then
        assertThat(campaignReport.status()).isEqualTo(FAILURE);
    }

    @Test
    public void should_start_scenario_execution() {
        // Given
        CampaignExecution campaignReport = new CampaignExecution(1L, "...", false, "", "#55:1", 5, "user");
        TestCase testCase = buildTestCase("1", "title");
        LocalDateTime beforeStartExecution = LocalDateTime.now().minusSeconds(1);

        // When
        campaignReport.initExecution(singletonList(testCase), "env", "user");

        // Then
        assertThat(campaignReport.scenarioExecutionReports()).hasSize(1);
        ScenarioExecutionCampaign startedScenarioExecution = campaignReport.scenarioExecutionReports().get(0);
        assertThat(startedScenarioExecution.scenarioId).isEqualTo(testCase.metadata().id());
        assertThat(startedScenarioExecution.scenarioName).isEqualTo(testCase.metadata().title());
        assertThat(startedScenarioExecution.execution.executionId()).isEqualTo(-1L);
        assertThat(startedScenarioExecution.execution.time()).isAfter(beforeStartExecution);
        assertThat(startedScenarioExecution.execution.status()).isEqualTo(NOT_EXECUTED);
        assertThat(startedScenarioExecution.execution.environment()).isEqualTo("env");
        assertThat(startedScenarioExecution.execution.datasetId()).isEqualTo(campaignReport.dataSetId);
        assertThat(startedScenarioExecution.execution.datasetVersion()).isEqualTo(campaignReport.dataSetVersion);
        assertThat(startedScenarioExecution.execution.user()).isEqualTo(campaignReport.userId);

        // When
        campaignReport.startScenarioExecution(testCase, "env", "user");

        // Then
        assertThat(campaignReport.scenarioExecutionReports()).hasSize(1);
        startedScenarioExecution = campaignReport.scenarioExecutionReports().get(0);
        assertThat(startedScenarioExecution.scenarioId).isEqualTo(testCase.metadata().id());
        assertThat(startedScenarioExecution.scenarioName).isEqualTo(testCase.metadata().title());
        assertThat(startedScenarioExecution.execution.executionId()).isEqualTo(-1L);
        assertThat(startedScenarioExecution.execution.time()).isAfter(beforeStartExecution);
        assertThat(startedScenarioExecution.execution.status()).isEqualTo(RUNNING);
        assertThat(startedScenarioExecution.execution.environment()).isEqualTo("env");
        assertThat(startedScenarioExecution.execution.user()).isEqualTo(campaignReport.userId);
        assertThat(startedScenarioExecution.execution.datasetId()).isEqualTo(campaignReport.dataSetId);
        assertThat(startedScenarioExecution.execution.datasetVersion()).isEqualTo(campaignReport.dataSetVersion);
    }

    @Test
    public void should_end_scenario_execution() {
        // Given
        CampaignExecution campaignReport = new CampaignExecution(1L, "...", false, "", null, null, "");
        TestCase testCase = buildTestCase("1", "title");
        campaignReport.initExecution(singletonList(testCase), "env", "user");
        campaignReport.startScenarioExecution(testCase, "env", "user");

        ScenarioExecutionCampaign scenarioReport_SUCCESS = buildScenarioReportFromMockedExecution(testCase.id(), testCase.metadata().title(), SUCCESS);

        // When
        campaignReport.endScenarioExecution(scenarioReport_SUCCESS);

        // Then
        assertThat(campaignReport.scenarioExecutionReports()).hasSize(1);
        assertThat(campaignReport.scenarioExecutionReports().get(0).execution.status()).isEqualTo(SUCCESS);
    }

    @Test
    public void should_compute_status_from_scenarios_when_end_campaign_execution() {
        // Given
        CampaignExecution campaignReport = new CampaignExecution(1L, "...", false, "", null, null, "");
        addScenarioExecutions(campaignReport, "1", "title1", SUCCESS);
        addScenarioExecutions(campaignReport, "2", "title2", FAILURE);

        // When
        campaignReport.endCampaignExecution();

        // Then
        assertThat(campaignReport.scenarioExecutionReports()).hasSize(2);
        assertThat(campaignReport.status()).isEqualTo(FAILURE);
    }

    @Test
    public void should_calculate_stop_final_status_when_having_not_executed_scenario() {
        // Given
        CampaignExecution campaignReport = new CampaignExecution(1L, "...", false, "", null, null, "");
        addScenarioExecutions(campaignReport, "1", "title1", SUCCESS);
        addScenarioExecutions(campaignReport, "2", "title2", NOT_EXECUTED);

        // When
        campaignReport.endCampaignExecution();

        // Then
        assertThat(campaignReport.scenarioExecutionReports()).hasSize(2);
        assertThat(campaignReport.status()).isEqualTo(STOPPED);
    }

    @Test
    public void should_calculate_filter_retry_scenario_in_status_calculation() {
        // Given
        String scenarioId = "1";
        ExecutionHistory.ExecutionSummary firstExecution = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(1L)
            .testCaseTitle("")
            .time(LocalDateTime.now().minusMinutes(1l))
            .duration(0l)
            .environment("")
            .user("")
            .status(FAILURE)
            .scenarioId("")
            .build();
        ScenarioExecutionCampaign firstReport = new ScenarioExecutionCampaign(scenarioId, "", firstExecution);
        ExecutionHistory.ExecutionSummary retryExecution = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .scenarioId("")
            .build();
        ScenarioExecutionCampaign retryReport = new ScenarioExecutionCampaign(scenarioId, "", retryExecution);
        CampaignExecution campaignReport = CampaignExecutionReportBuilder.builder()
            .addScenarioExecutionReport(firstReport)
            .addScenarioExecutionReport(retryReport)
            .build();
        // When
        ServerReportStatus status = campaignReport.status();

        // Then
        assertThat(status).isEqualTo(SUCCESS);
    }

    @Test
    public void should_compute_duration_for_one_scenario_execution() {
        LocalDateTime now = LocalDateTime.now();
        long duration = 3;

        List<ScenarioExecutionCampaign> executions = stubScenarioExecution(singletonList(now), singletonList(duration));
        CampaignExecution sut = fakeCampaignReport(executions);

        assertThat(sut.getDuration()).isEqualTo(3);
    }

    @Test
    public void should_compute_duration_for_two_scenarios_sequential_executions() {
        LocalDateTime now = LocalDateTime.now();
        long duration1 = 3;
        LocalDateTime scenarioExecutionStartDate2 = now.plus(duration1, ChronoUnit.MILLIS);
        long duration2 = 6;

        List<ScenarioExecutionCampaign> executions =
            stubScenarioExecution(
                asList(now, scenarioExecutionStartDate2),
                asList(duration1, duration2)
            );
        CampaignExecution sut = fakeCampaignReport(executions);

        assertThat(sut.getDuration()).isEqualTo(9);
    }

    @Test
    public void should_compute_duration_for_two_scenarios_parallel_executions() {
        LocalDateTime now = LocalDateTime.now();
        long duration1 = 3;
        long duration2 = 6;

        List<ScenarioExecutionCampaign> executions =
            stubScenarioExecution(
                asList(now, now),
                asList(duration1, duration2)
            );
        CampaignExecution sut = fakeCampaignReport(executions);

        assertThat(sut.getDuration()).isEqualTo(6);
    }

    private List<ScenarioExecutionCampaign> stubScenarioExecution(List<LocalDateTime> times, List<Long> durations) {
        ExecutionHistory.ExecutionSummary execution = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution.time()).thenReturn(times.get(0), times.subList(1, durations.size()).toArray(new LocalDateTime[0]));
        when(execution.duration()).thenReturn(durations.get(0), durations.subList(1, durations.size()).toArray(new Long[0]));

        ScenarioExecutionCampaign dto = new ScenarioExecutionCampaign("0", UUID.randomUUID().toString(), execution);
        List<ScenarioExecutionCampaign> reports = new ArrayList<>();
        IntStream.range(0, times.size()).forEach(i -> reports.add(dto));
        return reports;
    }

    private CampaignExecution fakeCampaignReport(List<ScenarioExecutionCampaign> executions) {
        return new CampaignExecution(1L, 1L, executions, "...", false, "", null, null, "");
    }

    private void addScenarioExecutions(CampaignExecution campaignReport, String scenarioId, String scenarioTitle, ServerReportStatus scenarioExecutionStatus) {
        TestCase testCase = buildTestCase(scenarioId, scenarioTitle);
        campaignReport.initExecution(singletonList(testCase), "", "");
        campaignReport.startScenarioExecution(testCase, "", "");

        ScenarioExecutionCampaign scenarioReport_FAILURE = buildScenarioReportFromMockedExecution(scenarioId, scenarioTitle, scenarioExecutionStatus);

        campaignReport.endScenarioExecution(scenarioReport_FAILURE);

        assertThat(campaignReport.status()).isEqualTo(RUNNING);
    }

    private ScenarioExecutionCampaign buildScenarioReportFromMockedExecution(String scenarioId, String scenarioTitle, ServerReportStatus scenarioExecutionStatus) {
        ExecutionHistory.ExecutionSummary execution = mock(ExecutionHistory.ExecutionSummary.class);
        when(execution.status()).thenReturn(scenarioExecutionStatus);
        return new ScenarioExecutionCampaign(scenarioId, scenarioTitle, execution);
    }

    private TestCase buildTestCase(String scenarioId, String scenarioTitle) {
        return GwtTestCase.builder()
            .withMetadata(
                TestCaseMetadataImpl.builder()
                    .withId(scenarioId)
                    .withTitle(scenarioTitle)
                    .build())
            .build();
    }
}

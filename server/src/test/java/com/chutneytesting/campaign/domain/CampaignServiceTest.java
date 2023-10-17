package com.chutneytesting.campaign.domain;


import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.FAILURE;
import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReportBuilder;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionCampaign;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CampaignServiceTest {
    @Test
    void should_return_campaign_report_by_campaign_execution_id() {
        // G
        CampaignRepository campaignRepository = mock(CampaignRepository.class);
        CampaignService campaignService = new CampaignService(campaignRepository);

        ExecutionHistory.ExecutionSummary execution1 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(1L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport1 = new ScenarioExecutionCampaign("scenario 1", "", execution1);
        ExecutionHistory.ExecutionSummary execution2 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport2 = new ScenarioExecutionCampaign("scenario 2", "", execution2);
        CampaignExecution campaignReport = CampaignExecutionReportBuilder.builder()
            .setCampaignId(42L)
            .setExecutionEnvironment("test env")
            .setPartialExecution(true)
            .setCampaignName("test name")
            .setExecutionId(43L)
            .setDataSetId("dataset id test")
            .setDataSetVersion(44)
            .setStartDate(LocalDateTime.MAX)
            .addScenarioExecutionReport(scenarioExecutionReport1)
            .addScenarioExecutionReport(scenarioExecutionReport2)
            .build();
        when(campaignRepository.findByExecutionId(anyLong())).thenReturn(campaignReport);

        // W
        CampaignExecution report = campaignService.findByExecutionId(0L);

        // T
        assertThat(report.scenarioExecutionReports()).hasSize(2);
        assertThat(report.status()).isEqualTo(SUCCESS);
        assertThat(report).usingRecursiveComparison()
            .isEqualTo(campaignReport);

    }

    @Test
    void should_return_campaign_report_by_campaign_execution_id_when_retry_scenario_executions_exist() {
        // G
        CampaignRepository campaignRepository = mock(CampaignRepository.class);
        CampaignService campaignService = new CampaignService(campaignRepository);

        String scenarioId = "scenario 1";
        ExecutionHistory.ExecutionSummary execution1 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(1L)
            .testCaseTitle("")
            .time(LocalDateTime.now().minusMinutes(1))
            .duration(0l)
            .environment("")
            .user("")
            .status(FAILURE)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport1 = new ScenarioExecutionCampaign(scenarioId, "", execution1);
        ExecutionHistory.ExecutionSummary execution2 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport2 = new ScenarioExecutionCampaign(scenarioId, "", execution2);
        CampaignExecution campaignReport = CampaignExecutionReportBuilder.builder()
            .addScenarioExecutionReport(scenarioExecutionReport1)
            .addScenarioExecutionReport(scenarioExecutionReport2)
            .build();
        when(campaignRepository.findByExecutionId(anyLong())).thenReturn(campaignReport);

        // W
        CampaignExecution report = campaignService.findByExecutionId(0L);

        // T
        assertThat(report.scenarioExecutionReports()).hasSize(1);
        assertThat(report.status()).isEqualTo(SUCCESS);
    }

    @Test
    void should_return_all_executions_of_a_campaign() {
        // Given
        Long campaignId = 1L;
        CampaignRepository campaignRepository = mock(CampaignRepository.class);
        ExecutionHistory.ExecutionSummary execution1 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(1L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport1 = new ScenarioExecutionCampaign("scenario 1", "", execution1);
        ExecutionHistory.ExecutionSummary execution2 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport2 = new ScenarioExecutionCampaign("scenario 2", "", execution2);
        List<CampaignExecution> allExecutions = List.of(
            CampaignExecutionReportBuilder.builder()
                .addScenarioExecutionReport(scenarioExecutionReport1)
                .build(),
            CampaignExecutionReportBuilder.builder()
                .addScenarioExecutionReport(scenarioExecutionReport2)
                .build()
        );
        when(campaignRepository.findExecutionsById(anyLong())).thenReturn(allExecutions);
        CampaignService sut = new CampaignService(campaignRepository);

        // When
        List<CampaignExecution> executionsReports = sut.findExecutionsById(campaignId);

        // Then
        assertThat(executionsReports).hasSameElementsAs(allExecutions);
    }

    @Test
    void should_return_all_executions_with_retries_of_a_campaign() {
        // Given
        Long campaignId = 1L;
        CampaignRepository campaignRepository = mock(CampaignRepository.class);
        String scenario1Id = "scenario 1";
        ExecutionHistory.ExecutionSummary execution1 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(1L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport1 = new ScenarioExecutionCampaign(scenario1Id, scenario1Id, execution1);
        ExecutionHistory.ExecutionSummary execution2 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        String scenario2Id = "scenario 2";
        ScenarioExecutionCampaign scenarioExecutionReport2 = new ScenarioExecutionCampaign(scenario2Id, "", execution2);
        ExecutionHistory.ExecutionSummary execution3 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(3L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport3 = new ScenarioExecutionCampaign("scenario 3", "", execution3);
        ExecutionHistory.ExecutionSummary execution4 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(4L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport4 = new ScenarioExecutionCampaign(scenario1Id, "", execution4);
        ExecutionHistory.ExecutionSummary execution5 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(5L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionCampaign scenarioExecutionReport5 = new ScenarioExecutionCampaign(scenario2Id, "", execution5);
        List<CampaignExecution> allExecutions = List.of(
            CampaignExecutionReportBuilder.builder()
                .setExecutionId(1L)
                .addScenarioExecutionReport(scenarioExecutionReport1)
                .addScenarioExecutionReport(scenarioExecutionReport4)
                .addScenarioExecutionReport(scenarioExecutionReport3)
                .build(),
            CampaignExecutionReportBuilder.builder()
                .setExecutionId(2L)
                .addScenarioExecutionReport(scenarioExecutionReport1)
                .addScenarioExecutionReport(scenarioExecutionReport2)
                .build(),
            CampaignExecutionReportBuilder.builder()
                .setExecutionId(3L)
                .addScenarioExecutionReport(scenarioExecutionReport3)
                .addScenarioExecutionReport(scenarioExecutionReport2)
                .addScenarioExecutionReport(scenarioExecutionReport5)
                .build()
        );
        when(campaignRepository.findExecutionsById(anyLong())).thenReturn(allExecutions);
        CampaignService sut = new CampaignService(campaignRepository);

        // When
        List<CampaignExecution> executionsReports = sut.findExecutionsById(campaignId);

        // Then
        assertThat(executionsReports).hasSize(3);
        assertThat(executionsReports.get(0).scenarioExecutionReports()).hasSize(2);
        assertThat(executionsReports.get(1).scenarioExecutionReports()).hasSize(2);
        assertThat(executionsReports.get(2).scenarioExecutionReports()).hasSize(2);
    }
}

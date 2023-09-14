package com.chutneytesting.campaign.domain;


import static com.chutneytesting.server.core.domain.execution.report.ServerReportStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReportBuilder;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.time.LocalDateTime;
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
        ScenarioExecutionReportCampaign scenarioExecutionReport1 = new ScenarioExecutionReportCampaign("scenario 1", "", execution1);
        ExecutionHistory.ExecutionSummary execution2 = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(2L)
            .testCaseTitle("")
            .time(LocalDateTime.now())
            .duration(0l)
            .environment("")
            .user("")
            .status(SUCCESS)
            .build();
        ScenarioExecutionReportCampaign scenarioExecutionReport2 = new ScenarioExecutionReportCampaign("scenario 2", "", execution2);
        CampaignExecutionReport campaignReport = CampaignExecutionReportBuilder.builder()
            .addScenarioExecutionReport(scenarioExecutionReport1)
            .addScenarioExecutionReport(scenarioExecutionReport2)
            .build();
        when(campaignRepository.findByExecutionId(anyLong())).thenReturn(campaignReport);

        // W
        CampaignExecutionReport report = campaignService.findByExecutionId(0L);

        // T
        assertThat(report.scenarioExecutionReports()).hasSize(2);
        assertThat(report.status()).isEqualTo(SUCCESS);
    }
}

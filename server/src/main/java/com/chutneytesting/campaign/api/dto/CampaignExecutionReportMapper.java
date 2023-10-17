package com.chutneytesting.campaign.api.dto;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.stream.Collectors;

public class CampaignExecutionReportMapper {

    public static CampaignExecutionReportDto toDto(CampaignExecution campaignReport) {
        return new CampaignExecutionReportDto(
            campaignReport.executionId,
            campaignReport.scenarioExecutionReports().stream()
                .map(ScenarioExecutionReportCampaignMapper::toDto)
                .collect(Collectors.toList()),
            campaignReport.campaignName,
            campaignReport.startDate,
            campaignReport.status(),
            campaignReport.partialExecution,
            campaignReport.executionEnvironment,
            campaignReport.userId,
            campaignReport.getDuration());
    }
}

package com.chutneytesting.campaign.api.dto;


import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;

public class ScenarioExecutionReportCampaignMapper {

    public static ScenarioExecutionReportOutlineDto toDto(ScenarioExecutionReportCampaign scenarioReport) {
        return new ScenarioExecutionReportOutlineDto(
            scenarioReport.scenarioId,
            scenarioReport.scenarioName,
            scenarioReport.execution
        );
    }

    public static ScenarioExecutionReportCampaign fromDto(ScenarioExecutionReportOutlineDto dto) {
        return new ScenarioExecutionReportCampaign(
            dto.getScenarioId(),
            dto.getScenarioName(),
            dto.getExecution()
        );
    }

}

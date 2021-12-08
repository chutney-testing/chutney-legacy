package com.chutneytesting.design.api.campaign.dto;

import static com.chutneytesting.tools.orient.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;
import static java.util.Optional.of;

import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;

public class ScenarioExecutionReportCampaignMapper {

    public static ScenarioExecutionReportOutlineDto toDto(ScenarioExecutionReportCampaign scenarioReport) {
        return new ScenarioExecutionReportOutlineDto(
            toFrontId(scenarioReport.scenarioId),
            scenarioReport.scenarioName,
            scenarioReport.execution
        );
    }

    public static ScenarioExecutionReportCampaign fromDto(ScenarioExecutionReportOutlineDto dto) {
        return new ScenarioExecutionReportCampaign(
            fromFrontId(of(dto.getScenarioId())),
            dto.getScenarioName(),
            dto.getExecution()
        );
    }

}

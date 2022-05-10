package com.chutneytesting.campaign.api.dto;

import static com.chutneytesting.tools.orient.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;
import static java.util.Optional.of;

import com.chutneytesting.campaign.domain.ScenarioExecutionReportCampaign;

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

package com.chutneytesting.design.api.campaign.dto;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.tools.ui.ComposableIdUtils;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class CampaignMapper {

    public static CampaignDto toDtoWithoutReport(Campaign campaign) {
        return new CampaignDto(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.scenarioIds.stream()
                .map(ComposableIdUtils::toFrontId)
                .collect(toList()),
            campaign.executionParameters,
            emptyList(),
            campaign.getStringScheduleTime(),
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            toFrontId(campaign.externalDatasetId));
    }

    public static CampaignDto toDto(Campaign campaign, List<CampaignExecutionReport> campaignExecutionReports) {
        return new CampaignDto(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.scenarioIds.stream()
                .map(ComposableIdUtils::toFrontId)
                .collect(toList()),
            campaign.executionParameters,
            reportToDto(campaignExecutionReports),
            campaign.getStringScheduleTime(),
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            toFrontId(campaign.externalDatasetId));
    }

    public static Campaign fromDto(CampaignDto dto) {
        return new Campaign(
            dto.getId(),
            dto.getTitle(),
            dto.getDescription(),
            dto.getScenarioIds().stream()
                .map(id -> fromFrontId(Optional.of(id)))
                .collect(toList()),
            dto.getComputedParameters(),
            dto.safeGetScheduleTime().map(LocalTime::parse).orElse(null),
            dto.getEnvironment(),
            dto.isParallelRun(),
            dto.isRetryAuto(),
            fromFrontId(dto.getDatasetId())
        );
    }

    private static List<CampaignExecutionReportDto> reportToDto(List<CampaignExecutionReport> campaignExecutionReports) {
        return campaignExecutionReports != null ? campaignExecutionReports.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .collect(toList()) : emptyList();
    }

}

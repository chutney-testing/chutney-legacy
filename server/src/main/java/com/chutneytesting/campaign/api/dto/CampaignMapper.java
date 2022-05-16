package com.chutneytesting.campaign.api.dto;

import static com.chutneytesting.tools.orient.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.orient.ComposableIdUtils.toFrontId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.campaign.domain.Campaign;
import com.chutneytesting.campaign.domain.CampaignExecutionReport;
import com.chutneytesting.tools.orient.ComposableIdUtils;
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
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            toFrontId(campaign.externalDatasetId),
            campaign.tags);
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
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            toFrontId(campaign.externalDatasetId),
            campaign.tags);
    }

    public static Campaign fromDto(CampaignDto dto) {
        return new Campaign(
            dto.getId(),
            dto.getTitle(),
            dto.getDescription(),
            dto.getScenarioIds().stream()
                .map(id -> fromFrontId(Optional.of(id)))
                .collect(toList()),
            dto.getExecutionParameters(),
            dto.getEnvironment(),
            dto.isParallelRun(),
            dto.isRetryAuto(),
            fromFrontId(dto.getDatasetId()),
            dto.getTags().stream().map(String::trim).map(String::toUpperCase).collect(toList())
        );
    }

    private static List<CampaignExecutionReportDto> reportToDto(List<CampaignExecutionReport> campaignExecutionReports) {
        return campaignExecutionReports != null ? campaignExecutionReports.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .collect(toList()) : emptyList();
    }
}

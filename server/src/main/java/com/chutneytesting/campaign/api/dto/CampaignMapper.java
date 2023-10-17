package com.chutneytesting.campaign.api.dto;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;

public class CampaignMapper {

    public static CampaignDto toDtoWithoutReport(Campaign campaign) {
        return new CampaignDto(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.scenarioIds,
            campaign.executionParameters,
            emptyList(),
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            campaign.externalDatasetId,
            campaign.tags);
    }

    public static CampaignDto toDto(Campaign campaign, List<CampaignExecution> campaignExecutions) {
        return new CampaignDto(
            campaign.id,
            campaign.title,
            campaign.description,
            campaign.scenarioIds,
            campaign.executionParameters,
            reportToDto(campaignExecutions),
            campaign.executionEnvironment(),
            campaign.parallelRun,
            campaign.retryAuto,
            campaign.externalDatasetId,
            campaign.tags);
    }

    public static Campaign fromDto(CampaignDto dto) {
        return new Campaign(
            dto.getId(),
            dto.getTitle(),
            dto.getDescription(),
            dto.getScenarioIds(),
            dto.getExecutionParameters(),
            dto.getEnvironment(),
            dto.isParallelRun(),
            dto.isRetryAuto(),
            dto.getDatasetId(),
            dto.getTags().stream().map(String::trim).map(String::toUpperCase).collect(toList())
        );
    }

    private static List<CampaignExecutionReportDto> reportToDto(List<CampaignExecution> campaignExecutions) {
        return campaignExecutions != null ? campaignExecutions.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .collect(toList()) : emptyList();
    }
}

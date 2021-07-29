package com.chutneytesting.design.api.campaign.dto;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.tools.ui.ComposableIdUtils;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Optional;

public class CampaignMapper {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("HH")
        .appendLiteral(":")
        .appendPattern("mm")
        .toFormatter();


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
            scheduleTimeToString(campaign.scheduleTime),
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
            scheduleTimeToString(campaign.scheduleTime),
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
            dto.safeGetScheduleTime().map(LocalTime::parse).orElse(null),
            dto.getEnvironment(),
            dto.isParallelRun(),
            dto.isRetryAuto(),
            fromFrontId(dto.getDatasetId()),
            dto.getTags().stream().map(String::trim).map(String::toUpperCase).collect(toList())
        );
    }

    private static String scheduleTimeToString(LocalTime scheduleTime) {
        return scheduleTime != null ? scheduleTime.format(FORMATTER) : null;
    }

    private static List<CampaignExecutionReportDto> reportToDto(List<CampaignExecutionReport> campaignExecutionReports) {
        return campaignExecutionReports != null ? campaignExecutionReports.stream()
            .map(CampaignExecutionReportMapper::toDto)
            .collect(toList()) : emptyList();
    }

}

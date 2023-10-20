package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CampaignExecutionApiMapper {
    CampaignExecutionReportSummaryDto toCompaignExecutionReportSummaryDto(CampaignExecution campaignExecution);

    default String mapOptionalString(Optional<String> value) {
        return value.orElse(null);
    }
}

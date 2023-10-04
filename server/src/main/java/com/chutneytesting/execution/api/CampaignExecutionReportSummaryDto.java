package com.chutneytesting.execution.api;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.time.LocalDateTime;

public record CampaignExecutionReportSummaryDto(Long executionId, String campaignName, LocalDateTime startDate,
                                                ServerReportStatus status, String dataSetId) {
}

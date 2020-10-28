package com.chutneytesting.instrument.domain;

import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.util.List;
import java.util.Map;

public interface ChutneyMetrics {

    void onScenarioExecutionEnded(String scenarioId, List<String> tags, ServerReportStatus status, long duration);

    void onCampaignExecutionEnded(String campaignId, ServerReportStatus status, Long campaignDuration, Map<ServerReportStatus, Long> scenarioCountByStatus);
}

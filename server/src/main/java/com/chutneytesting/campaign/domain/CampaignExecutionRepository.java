package com.chutneytesting.campaign.domain;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CampaignExecutionRepository {
    Optional<CampaignExecutionReport> currentExecution(Long campaignId);

    List<CampaignExecutionReport> currentExecutions();

    void startExecution(Long campaignId, CampaignExecutionReport campaignExecutionReport);

    void stopExecution(Long campaignId);

    CampaignExecutionReport getLastExecutionReport(Long campaignId);

    CampaignExecutionReport deleteExecution(Long executionId);
    Set<CampaignExecutionReport> deleteExecutions(Set<Long> executionsIds);
}

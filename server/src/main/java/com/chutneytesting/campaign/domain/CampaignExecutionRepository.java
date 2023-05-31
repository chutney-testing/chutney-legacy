package com.chutneytesting.campaign.domain;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;
import java.util.Optional;

public interface CampaignExecutionRepository {
    Optional<CampaignExecutionReport> currentExecution(Long campaignId);

    List<CampaignExecutionReport> currentExecutions();

    void startExecution(Long campaignId, CampaignExecutionReport campaignExecutionReport);

    void stopExecution(Long campaignId);
}

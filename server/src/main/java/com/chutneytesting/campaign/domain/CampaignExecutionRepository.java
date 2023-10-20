package com.chutneytesting.campaign.domain;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CampaignExecutionRepository {
    Optional<CampaignExecution> currentExecution(Long campaignId);

    List<CampaignExecution> currentExecutions();

    void startExecution(Long campaignId, CampaignExecution campaignExecution);

    void stopExecution(Long campaignId);

    CampaignExecution getLastExecution(Long campaignId);

    void deleteExecutions(Set<Long> executionsIds);
}

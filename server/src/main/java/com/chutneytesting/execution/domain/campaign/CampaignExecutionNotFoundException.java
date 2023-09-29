package com.chutneytesting.execution.domain.campaign;

import java.util.Optional;

public class CampaignExecutionNotFoundException extends RuntimeException {
    public CampaignExecutionNotFoundException(Long campaignExecutionId, Optional<Long> campaignId) {
        super("Campaign execution [" + campaignExecutionId + "] could not be found" + campaignId.map(id -> " for campaign id [" + id + "]"));
    }

    public CampaignExecutionNotFoundException(Long campaignId) {
        super("Campaign execution could not be found for campaign id [" + campaignId + "]");
    }
}

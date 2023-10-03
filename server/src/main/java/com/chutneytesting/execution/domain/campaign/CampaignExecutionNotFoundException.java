package com.chutneytesting.execution.domain.campaign;

public class CampaignExecutionNotFoundException extends RuntimeException {
    public CampaignExecutionNotFoundException(Long campaignId) {
        super("Campaign execution could not be found for campaign id [" + campaignId + "]");
    }

    public CampaignExecutionNotFoundException(Long campaignId, Long campaignExecutionId) {
        super("Campaign execution [" + campaignExecutionId + "] could not be found" + (campaignId != null ? " for campaign id [" + campaignId + "]" : ""));
    }
}

package com.chutneytesting.execution.domain.campaign;

public class CampaignExecutionNotFoundException extends RuntimeException {
    public CampaignExecutionNotFoundException(Long campaignExecutionId) {
        super("Campaign execution ["+campaignExecutionId+"] could not be found");
    }
}

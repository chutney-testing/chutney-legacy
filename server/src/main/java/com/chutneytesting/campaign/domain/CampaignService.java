package com.chutneytesting.campaign.domain;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;

public class CampaignService {


    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {

        this.campaignRepository = campaignRepository;
    }

    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        return campaignRepository.findByExecutionId(campaignExecutionId);
    }
}

package com.chutneytesting.campaign.domain;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecution;
import java.util.List;

public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public CampaignExecution findByExecutionId(Long campaignExecutionId) {
        CampaignExecution report = campaignRepository.findByExecutionId(campaignExecutionId);
        return report.withoutRetries();
    }

    public List<CampaignExecution> findExecutionsById(Long campaignId) {
        return campaignRepository.findExecutionsById(campaignId).stream()
            .map(CampaignExecution::withoutRetries)
            .collect(toList());
    }
}

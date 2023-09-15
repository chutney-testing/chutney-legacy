package com.chutneytesting.campaign.domain;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import java.util.List;

public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    public CampaignExecutionReport findByExecutionId(Long campaignExecutionId) {
        CampaignExecutionReport report = campaignRepository.findByExecutionId(campaignExecutionId);
        return report.withoutRetries();
    }

    public List<CampaignExecutionReport> findExecutionsById(Long campaignId) {
        return campaignRepository.findExecutionsById(campaignId).stream()
            .map(CampaignExecutionReport::withoutRetries)
            .collect(toList());
    }
}

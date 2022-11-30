package com.chutneytesting.execution.api.schedule;

import static com.chutneytesting.ServerConfiguration.SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE;

import com.chutneytesting.execution.domain.schedule.CampaignScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleCampaign {

    private final CampaignScheduler campaignScheduler;

    public ScheduleCampaign(CampaignScheduler campaignScheduler) {
        this.campaignScheduler = campaignScheduler;
    }

    @Scheduled(fixedRateString = SCHEDULED_CAMPAIGNS_FIXED_RATE_SPRING_VALUE)
    public void executeScheduledCampaign() {
        campaignScheduler.executeScheduledCampaigns();
    }
}

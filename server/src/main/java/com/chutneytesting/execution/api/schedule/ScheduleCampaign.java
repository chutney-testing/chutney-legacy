package com.chutneytesting.execution.api.schedule;

import com.chutneytesting.execution.domain.schedule.CampaignScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleCampaign {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleCampaign.class);

    private final CampaignScheduler campaignScheduler;

    public ScheduleCampaign(CampaignScheduler campaignScheduler) {
        this.campaignScheduler = campaignScheduler;
    }

    @Scheduled(fixedRate = 60000)
    public void executeScheduledCampaign() {
        campaignScheduler.executeScheduledCampaign();
    }
}

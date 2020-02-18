package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CampaignScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    private LocalTime lastExecutionTime = LocalTime.now().minusMinutes(10);
    private final CampaignExecutionEngine campaignExecutionEngine;
    private final SchedulerRepository schedulerRepository;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, SchedulerRepository schedulerRepository) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.schedulerRepository = schedulerRepository;
    }

    @Async
    public void executeScheduledCampaign() {
        final List<Long> campaignIds = checkCampaignToExecute();

        campaignIds.stream()
            .parallel()
            .forEach(c -> {
                    LOGGER.info("Execute campaign with id [{}]", c);
                    try {
                        campaignExecutionEngine.executeById(c);
                    } catch (Exception e) {
                        LOGGER.error("Error during campaign execution", e);
                    }
                }
            );
    }

    private List<Long> checkCampaignToExecute() {
        final LocalTime newLocalTime = LocalTime.now();
        final List<Long> campaignIdsToExecute = schedulerRepository.getCampaignScheduledAfter(lastExecutionTime);
        lastExecutionTime = newLocalTime;
        return campaignIdsToExecute;
    }
}

package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CampaignScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    private LocalDateTime lastExecutionDateTime = LocalDateTime.now().minusMinutes(10);
    private final CampaignExecutionEngine campaignExecutionEngine;
    private final SchedulerRepository schedulerRepository;
    private final Clock clock;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, SchedulerRepository schedulerRepository, Clock clock) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.schedulerRepository = schedulerRepository;
        this.clock = clock;
    }

    @Async
    public void executeScheduledCampaign() {
        final List<Long> campaignIds = checkCampaignToExecute();

        campaignIds.stream()
            .parallel()
            .forEach(c -> {
                    LOGGER.info("Execute campaign with id [{}]", c);
                    try {
                        campaignExecutionEngine.executeById(c, "auto");
                    } catch (Exception e) {
                        LOGGER.error("Error during campaign execution", e);
                    }
                }
            );
    }

    private List<Long> checkCampaignToExecute() {
        final LocalDateTime newLocalDateTime = LocalDateTime.now(clock);
        final List<Long> campaignIdsToExecute = schedulerRepository.getCampaignScheduledAfter(lastExecutionDateTime);
        lastExecutionDateTime = newLocalDateTime;
        return campaignIdsToExecute;
    }
}

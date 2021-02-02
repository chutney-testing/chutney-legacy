package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.design.domain.campaign.SchedulingCampaign;
import com.chutneytesting.design.domain.campaign.SchedulingCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
    private final SchedulingCampaignRepository schedulingCampaignRepository;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, SchedulerRepository schedulerRepository, Clock clock, SchedulingCampaignRepository schedulingCampaignRepository) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.schedulerRepository = schedulerRepository;
        this.clock = clock;
        this.schedulingCampaignRepository = schedulingCampaignRepository;
    }

    /**
     * TODO Do we want to specify a pool size too for parallel execution ?
     * **/
    @Async
    public void executeScheduledCampaign() {
        final List<Long> campaignIds = checkCampaignToExecutePeriodically();
        campaignIds.addAll(checkScheduleCampaign());

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

    private List<Long> checkScheduleCampaign() {
        List<SchedulingCampaign> ids = schedulingCampaignRepository.getALl()
            .stream()
            .filter(sc -> sc.schedulingDate.isBefore(LocalDateTime.now()))
            .collect(Collectors.toList());

        ids.forEach( sc -> schedulingCampaignRepository.removeById(sc.id));

        return ids.stream().map(sc -> sc.campaignId).collect(Collectors.toList());
    }

    private List<Long> checkCampaignToExecutePeriodically() {
        final LocalDateTime newLocalDateTime = LocalDateTime.now(clock);
        final List<Long> campaignIdsToExecute = schedulerRepository.getCampaignScheduledAfter(lastExecutionDateTime);
        lastExecutionDateTime = newLocalDateTime;
        return campaignIdsToExecute;
    }
}

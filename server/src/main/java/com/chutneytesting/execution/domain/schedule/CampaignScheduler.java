package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.design.domain.campaign.FREQUENCY;
import com.chutneytesting.design.domain.campaign.ScheduledCampaign;
import com.chutneytesting.design.domain.campaign.ScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CampaignScheduler {

    public static final String SCHEDULER_EXECUTE_USER = "auto";
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    private LocalDateTime timeScheduledCampaignsLastExecution;
    private final CampaignExecutionEngine campaignExecutionEngine;
    private final TimeScheduledCampaignRepository timeScheduledCampaignRepository;
    private final ScheduledCampaignRepository scheduledCampaignRepository;
    private final Clock clock;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, TimeScheduledCampaignRepository timeScheduledCampaignRepository, Clock clock, ScheduledCampaignRepository scheduledCampaignRepository) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.timeScheduledCampaignRepository = timeScheduledCampaignRepository;
        this.clock = clock;
        this.scheduledCampaignRepository = scheduledCampaignRepository;

        this.timeScheduledCampaignsLastExecution = LocalDateTime.now(this.clock).minusMinutes(10);
    }

    @Async
    public void executeScheduledCampaigns() {
        Stream.concat(
            timeScheduledCampaignIdsToExecute(),
            scheduledCampaignIdsToExecute()
        )
            .parallel()
            .forEach(this::executeScheduledCampaignById);
    }

    public LocalDateTime timeScheduledCampaignsLastExecution() {
        return timeScheduledCampaignsLastExecution;
    }

    private void executeScheduledCampaignById(Long campaignId) {
        LOGGER.info("Execute campaign with id [{}]", campaignId);
        try {
            campaignExecutionEngine.executeById(campaignId, SCHEDULER_EXECUTE_USER);
        } catch (Exception e) {
            LOGGER.error("Error during campaign [{}] execution", campaignId, e);
        }
    }

    private Stream<Long> scheduledCampaignIdsToExecute() {
        try {
            return scheduledCampaignRepository.getALl().stream()
                .filter(sc -> sc.scheduledDate.isBefore(LocalDateTime.now(clock)))
                .peek(this::prepareScheduledCampaignForNextExecution)
                .map(sc -> sc.campaignId);
        } catch (Exception e) {
            LOGGER.error("Error retrieving scheduled campaigns", e);
            return Stream.empty();
        }
    }

    private void prepareScheduledCampaignForNextExecution(ScheduledCampaign scheduledCampaign) {
        try {
            if (!FREQUENCY.EMPTY.equals(scheduledCampaign.frequency)) {
                scheduledCampaignRepository.add(scheduledCampaign.nextScheduledExecution());
            }
            scheduledCampaignRepository.removeById(scheduledCampaign.id);
        } catch (Exception e) {
            LOGGER.error("Error preparing scheduled campaign next execution [{}]", scheduledCampaign.id, e);
        }
    }

    private Stream<Long> timeScheduledCampaignIdsToExecute() {
        try {
            final LocalDateTime newLocalDateTime = LocalDateTime.now(clock);
            final List<Long> campaignIdsToExecute = timeScheduledCampaignRepository.getCampaignScheduledAfter(timeScheduledCampaignsLastExecution);
            timeScheduledCampaignsLastExecution = newLocalDateTime;
            return campaignIdsToExecute.stream();
        } catch (Exception e) {
            LOGGER.error("Error retrieving time scheduled campaigns", e);
            return Stream.empty();
        }
    }
}

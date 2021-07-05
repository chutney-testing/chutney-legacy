package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.design.domain.campaign.Frequency;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaign;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaignRepository;
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

    private LocalDateTime dailyScheduledCampaignsLastExecution;
    private final CampaignExecutionEngine campaignExecutionEngine;
    private final DailyScheduledCampaignRepository dailyScheduledCampaignRepository;
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository;
    private final Clock clock;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, DailyScheduledCampaignRepository dailyScheduledCampaignRepository, Clock clock, PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.dailyScheduledCampaignRepository = dailyScheduledCampaignRepository;
        this.clock = clock;
        this.periodicScheduledCampaignRepository = periodicScheduledCampaignRepository;

        this.dailyScheduledCampaignsLastExecution = LocalDateTime.now(this.clock).minusMinutes(10);
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

    public LocalDateTime dailyScheduledCampaignsLastExecution() {
        return dailyScheduledCampaignsLastExecution;
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
            return periodicScheduledCampaignRepository.getALl().stream()
                .filter(sc -> sc.nextExecutionDate.isBefore(LocalDateTime.now(clock)))
                .peek(this::prepareScheduledCampaignForNextExecution)
                .map(sc -> sc.campaignId);
        } catch (Exception e) {
            LOGGER.error("Error retrieving scheduled campaigns", e);
            return Stream.empty();
        }
    }

    private void prepareScheduledCampaignForNextExecution(PeriodicScheduledCampaign periodicScheduledCampaign) {
        try {
            if (!Frequency.EMPTY.equals(periodicScheduledCampaign.frequency)) {
                periodicScheduledCampaignRepository.add(periodicScheduledCampaign.nextScheduledExecution());
            }
            periodicScheduledCampaignRepository.removeById(periodicScheduledCampaign.id);
        } catch (Exception e) {
            LOGGER.error("Error preparing scheduled campaign next execution [{}]", periodicScheduledCampaign.id, e);
        }
    }

    private Stream<Long> timeScheduledCampaignIdsToExecute() {
        try {
            final LocalDateTime newLocalDateTime = LocalDateTime.now(clock);
            final List<Long> campaignIdsToExecute = dailyScheduledCampaignRepository.getCampaignScheduledAfter(dailyScheduledCampaignsLastExecution);
            dailyScheduledCampaignsLastExecution = newLocalDateTime;
            return campaignIdsToExecute.stream();
        } catch (Exception e) {
            LOGGER.error("Error retrieving time scheduled campaigns", e);
            return Stream.empty();
        }
    }
}

package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.design.domain.campaign.Frequency;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaign;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CampaignScheduler {

    public static final String SCHEDULER_EXECUTE_USER = "auto";
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    private final CampaignExecutionEngine campaignExecutionEngine;
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository;
    private final Clock clock;
    private final ExecutorService executor;

    public CampaignScheduler(
        CampaignExecutionEngine campaignExecutionEngine,
        Clock clock,
        PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository,
        @Qualifier("scheduledCampaignsExecutor") ExecutorService executor
    ) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.clock = clock;
        this.periodicScheduledCampaignRepository = periodicScheduledCampaignRepository;
        this.executor = executor;
    }

    @Async
    public void executeScheduledCampaigns() {
        try {
            executor.invokeAll(
                scheduledCampaignIdsToExecute()
                    .map(this::executeScheduledCampaignById)
                    .collect(Collectors.toList())
            );
        } catch (InterruptedException e) {
            LOGGER.error("Scheduled campaigns thread interrupted", e);
        }
    }

    private Callable<Void> executeScheduledCampaignById(Long campaignId) {
        return () -> {
            LOGGER.info("Execute campaign with id [{}]", campaignId);
            try {
                campaignExecutionEngine.executeById(campaignId, SCHEDULER_EXECUTE_USER);
            } catch (Exception e) {
                LOGGER.error("Error during campaign [{}] execution", campaignId, e);
            }
            return null;
        };
    }

    synchronized private Stream<Long> scheduledCampaignIdsToExecute() {
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
                LOGGER.info("Next execution of scheduled campaign [{}] with frequency [{}] has been added", periodicScheduledCampaign.campaignId, periodicScheduledCampaign.frequency);
            }
            periodicScheduledCampaignRepository.removeById(periodicScheduledCampaign.id);
        } catch (Exception e) {
            LOGGER.error("Error preparing scheduled campaign next execution [{}]", periodicScheduledCampaign.id, e);
        }
    }
}

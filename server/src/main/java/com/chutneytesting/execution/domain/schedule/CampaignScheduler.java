package com.chutneytesting.execution.domain.schedule;

import static java.util.Collections.emptyList;

import com.chutneytesting.design.domain.campaign.Frequency;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaign;
import com.chutneytesting.design.domain.campaign.PeriodicScheduledCampaignRepository;
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

    public static final String SCHEDULER_EXECUTE_USER = "auto";
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignScheduler.class);

    private final CampaignExecutionEngine campaignExecutionEngine;
    private final PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository;
    private final Clock clock;

    public CampaignScheduler(CampaignExecutionEngine campaignExecutionEngine, Clock clock, PeriodicScheduledCampaignRepository periodicScheduledCampaignRepository) {
        this.campaignExecutionEngine = campaignExecutionEngine;
        this.clock = clock;
        this.periodicScheduledCampaignRepository = periodicScheduledCampaignRepository;
    }

    @Async
    public void executeScheduledCampaigns() {
        scheduledCampaignIdsToExecute()
            .parallelStream()
            .forEach(this::executeScheduledCampaignById);
    }

    private void executeScheduledCampaignById(Long campaignId) {
        LOGGER.info("Execute campaign with id [{}]", campaignId);
        try {
            campaignExecutionEngine.executeById(campaignId, SCHEDULER_EXECUTE_USER);
        } catch (Exception e) {
            LOGGER.error("Error during campaign [{}] execution", campaignId, e);
        }
    }

    private List<Long> scheduledCampaignIdsToExecute() {
        try {
            return periodicScheduledCampaignRepository.getALl().stream()
                .filter(sc -> sc.nextExecutionDate.isBefore(LocalDateTime.now(clock)))
                .peek(this::prepareScheduledCampaignForNextExecution)
                .map(sc -> sc.campaignId)
                .collect(Collectors.toUnmodifiableList());
        } catch (Exception e) {
            LOGGER.error("Error retrieving scheduled campaigns", e);
            return emptyList();
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
}

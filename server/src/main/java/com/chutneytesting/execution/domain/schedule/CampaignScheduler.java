/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.execution.domain.schedule;

import com.chutneytesting.campaign.domain.Frequency;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaign;
import com.chutneytesting.campaign.domain.PeriodicScheduledCampaignRepository;
import com.chutneytesting.execution.domain.campaign.CampaignExecutionEngine;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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

    @Async("scheduleCampaignsExecutor")
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

    private Callable<Void> executeScheduledCampaignById(List<Long> campaignsId) {
        return () -> {
            campaignsId.forEach(campaignId -> {
                try {
                    LOGGER.info("Execute campaign with id [{}]", campaignId);
                    campaignExecutionEngine.executeById(campaignId, SCHEDULER_EXECUTE_USER);
                } catch (Exception e) {
                    LOGGER.error("Error during campaign [{}] execution", campaignId, e);
                }
            });
            return null;
        };
    }

    synchronized private Stream<List<Long>> scheduledCampaignIdsToExecute() {
        try {
            return periodicScheduledCampaignRepository.getALl().stream()
                .filter(sc -> sc.nextExecutionDate != null)
                .filter(sc -> sc.nextExecutionDate.isBefore(LocalDateTime.now(clock)))
                .peek(this::prepareScheduledCampaignForNextExecution)
                .map(sc -> sc.campaignsId);
        } catch (Exception e) {
            LOGGER.error("Error retrieving scheduled campaigns", e);
            return Stream.empty();
        }
    }

    private void prepareScheduledCampaignForNextExecution(PeriodicScheduledCampaign periodicScheduledCampaign) {
        try {
            if (!Frequency.EMPTY.equals(periodicScheduledCampaign.frequency)) {
                periodicScheduledCampaignRepository.add(periodicScheduledCampaign.nextScheduledExecution());
                LOGGER.info("Next execution of scheduled campaign(s) {} with frequency [{}] has been added", periodicScheduledCampaign.campaignsId, periodicScheduledCampaign.frequency);
            }
            periodicScheduledCampaignRepository.removeById(periodicScheduledCampaign.id);
        } catch (Exception e) {
            LOGGER.error("Error preparing scheduled campaign next execution [{}]", periodicScheduledCampaign.id, e);
        }
    }
}

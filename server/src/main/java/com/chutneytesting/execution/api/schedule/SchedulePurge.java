package com.chutneytesting.execution.api.schedule;

import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_CRON_SPRING_VALUE;

import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulePurge {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulePurge.class);
    private final PurgeService purgeService;

    public SchedulePurge(PurgeService purgeService) {
        this.purgeService = purgeService;
    }

    @Scheduled(cron = SCHEDULED_PURGE_CRON_SPRING_VALUE)
    public void launchPurge() {
        LOGGER.debug("Launch executions purge : START");
        try {
            CompletableFuture.supplyAsync(purgeService::purge).get(10, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Purge did not finish correctly.", e);
        }
        LOGGER.debug("Launch executions purge : END");
    }
}

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

package com.chutneytesting.execution.api.schedule;

import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_CRON_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_RETRY_COUNT_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_TIMEOUT_SPRING_VALUE;
import static java.util.Optional.empty;

import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import com.chutneytesting.server.core.domain.execution.history.PurgeService.PurgeReport;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulePurge {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulePurge.class);
    private final PurgeService purgeService;
    private final Integer timeout;
    private final Integer maxRetries;

    /**
     * @param purgeService  The purge service implementation to use
     * @param timeout       The timeout in seconds allowed to execute purge and all its potentials retries
     * @param maxRetries    The maximum retries to attempt in order to have a purge without exception
     */
    public SchedulePurge(
        PurgeService purgeService,
        @Value(SCHEDULED_PURGE_TIMEOUT_SPRING_VALUE) Integer timeout,
        @Value(SCHEDULED_PURGE_RETRY_COUNT_SPRING_VALUE) Integer maxRetries
    ) {
        this.purgeService = purgeService;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    @Scheduled(cron = SCHEDULED_PURGE_CRON_SPRING_VALUE)
    public Optional<PurgeReport> launchPurge() {
        try {
            LOGGER.debug("Launch executions purge : START");
            return Optional.of(
                retryExceptionallyAsync(purgeService::purge, maxRetries)
                    .get(timeout, TimeUnit.SECONDS) // Note here that timeout is for all purge execution (first exec + retries)
            );
        } catch (InterruptedException | ExecutionException | TimeoutException | RuntimeException e) {
            LOGGER.error("Purge did not finish correctly.", e);
        } finally {
            LOGGER.debug("Launch executions purge : END");
        }
        return empty();
    }

    private <T> CompletableFuture<T> retryExceptionallyAsync(Supplier<T> supplier, int maxRetries) {
        CompletableFuture<T> cf = CompletableFuture.supplyAsync(supplier);
        for (int i = 0; i < maxRetries; i++) {
            int finalI = i;
            cf = cf.exceptionallyAsync(throwable -> {
                LOGGER.debug("Retry executions purge : {}", finalI);
                return supplier.get();
            });
        }
        return cf;
    }
}

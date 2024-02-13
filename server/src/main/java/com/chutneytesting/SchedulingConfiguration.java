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

package com.chutneytesting;

import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_MAX_CAMPAIGN_EXECUTIONS_SPRING_VALUE;
import static com.chutneytesting.ServerConfigurationValues.SCHEDULED_PURGE_MAX_SCENARIO_EXECUTIONS_SPRING_VALUE;

import com.chutneytesting.campaign.domain.CampaignExecutionRepository;
import com.chutneytesting.campaign.domain.CampaignRepository;
import com.chutneytesting.execution.api.schedule.ScheduleCampaign;
import com.chutneytesting.execution.domain.PurgeServiceImpl;
import com.chutneytesting.execution.domain.schedule.CampaignScheduler;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.PurgeService;
import com.chutneytesting.server.core.domain.scenario.TestCaseRepository;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfiguration implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingConfiguration.class);

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) -> {
            LOGGER.error("Uncaught exception in async execution", ex);
        };
    }

    @Bean
    public TaskScheduler taskScheduler() {
        var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.setThreadNamePrefix("task-exec");
        return threadPoolTaskScheduler;
    }

    /**
     * Default task executor for @Async (used for SSE for example)
     * With a default  with default configuration: org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Pool
     */
    @Bean
    public TaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
        return builder.threadNamePrefix("app-task-exec").build();
    }

    @Bean
    public ApplicationRunner scheduledMissedCampaignToExecute(CampaignScheduler campaignScheduler) {
        return arg -> campaignScheduler.scheduledMissedCampaignIds();
    }

    /**
     * @see ScheduleCampaign#executeScheduledCampaign()
     */
    @Bean
    public TaskExecutor scheduleCampaignsExecutor() {
        return new SimpleAsyncTaskExecutor("schedule-campaigns-executor");
    }

    /**
     * @see CampaignScheduler#executeScheduledCampaigns()
     */
    @Bean
    public ExecutorService scheduledCampaignsExecutor(@Value(SCHEDULED_CAMPAIGNS_EXECUTOR_POOL_SIZE_SPRING_VALUE) Integer threadForScheduledCampaigns) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadForScheduledCampaigns);
        executor.setMaxPoolSize(threadForScheduledCampaigns);
        executor.setThreadNamePrefix("scheduled-campaigns-executor");
        executor.initialize();
        LOGGER.debug("Pool for scheduled campaigns created with size {}", threadForScheduledCampaigns);
        return new ExecutorServiceAdapter(executor);
    }

    @Bean
    public PurgeService purgeService(
        TestCaseRepository testCaseRepository,
        ExecutionHistoryRepository executionRepository,
        CampaignRepository campaignRepository,
        CampaignExecutionRepository campaignExecutionRepository,
        @Value(SCHEDULED_PURGE_MAX_SCENARIO_EXECUTIONS_SPRING_VALUE) Integer maxScenarioExecutionsConfig,
        @Value(SCHEDULED_PURGE_MAX_CAMPAIGN_EXECUTIONS_SPRING_VALUE) Integer maxCampaignExecutionsConfig
    ) {
        return new PurgeServiceImpl(
            testCaseRepository,
            executionRepository,
            campaignRepository,
            campaignExecutionRepository,
            maxScenarioExecutionsConfig,
            maxCampaignExecutionsConfig
        );
    }
}

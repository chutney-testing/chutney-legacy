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

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Located by the <b>spring-boot-maven-plugin</b> Maven plugin.
 */
public class ServerBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String... args) {
        final ConfigurableApplicationContext context = start(args);
        cleanApplicationState(context);
    }

    public static ConfigurableApplicationContext start(String... args) {
        SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(ServerConfiguration.class)
            .registerShutdownHook(true)
            .bannerMode(Mode.OFF);

        return appBuilder.build().run(args);
    }

    private static void cleanApplicationState(ConfigurableApplicationContext context) {
        int staleExecutionCount = context.getBean(ExecutionHistoryRepository.class).setAllRunningExecutionsToKO();
        LOGGER.info("Starting with " + staleExecutionCount + " unfinished executions");
    }
}

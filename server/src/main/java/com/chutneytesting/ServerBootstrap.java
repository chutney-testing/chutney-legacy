package com.chutneytesting;

import com.chutneytesting.admin.domain.gitbackup.GitBackupService;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Located by the <b>spring-boot-maven-plugin</b> Maven plugin.
 */
public class ServerBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String... args) {
        final ConfigurableApplicationContext context = start(args);
        cleanApplicationState(context);
        registerContextClosedEvent(context);
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

    private static void registerContextClosedEvent(ConfigurableApplicationContext context) {
        context.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
            LOGGER.info("Git backup started...");
            context.getBean(GitBackupService.class).export();
            LOGGER.info("Git backup ended");
        });
    }
}

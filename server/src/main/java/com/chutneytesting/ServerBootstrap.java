package com.chutneytesting;

import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Located by the <b>spring-boot-maven-plugin</b> Maven plugin.
 */
public class ServerBootstrap {

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
        context.getBean(ExecutionHistoryRepository.class).setAllRunningExecutionsToKO();
    }
}

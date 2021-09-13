package com.chutneytesting;

import com.chutneytesting.admin.domain.gitbackup.GitBackupService;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.orientechnologies.orient.core.Orient;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
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
        registerShutdownHooks(context);
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

    private static void registerShutdownHooks(ConfigurableApplicationContext context) {
        try {
            Set<Thread> thirdPartyHooks = copyThirdPartyHooks();
            removeThirdPartyHooks(thirdPartyHooks);

            Set<Thread> chutneyHooks = chutneyHooks(context);
            Thread oneHookToRuleThemAll = new Thread(() -> hookThemAll(chutneyHooks, thirdPartyHooks));
            Runtime.getRuntime().addShutdownHook(oneHookToRuleThemAll);
        } catch (Exception e) {
            LOGGER.warn("Error while setting shutdown hooks");
        }
    }

    private static Set<Thread> copyThirdPartyHooks() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> appShutdownHooks = Class.forName("java.lang.ApplicationShutdownHooks");
        Field hooksField = appShutdownHooks.getDeclaredField("hooks");
        hooksField.setAccessible(true);
        return new HashMap<>((IdentityHashMap<Thread, Thread>) hooksField.get(null)).keySet();
    }

    private static void removeThirdPartyHooks(Set<Thread> thirdPartyHooks) {
        thirdPartyHooks.forEach(t -> {
            if (!Runtime.getRuntime().removeShutdownHook(t)) {
                LOGGER.warn("Unable to delay 3rd party shutdown hook: " + t.getName() + " from " + t.getClass().getCanonicalName() + ". This might impede Chutney's own hooks to run properly.");
            }
        });
    }

    private static Set<Thread> chutneyHooks(ConfigurableApplicationContext context) {
        Set<Thread> chutneyHooks = new HashSet<>();
        chutneyHooks.add(new Thread(() -> context.getBean(GitBackupService.class).export()));
        return chutneyHooks;
    }

    private static void hookThemAll(Set<Thread> chutneyHooks, Set<Thread> thirdPartyHooks) {
        LOGGER.info("Shutdown started...");
        chutneyHooks.forEach(ServerBootstrap::run);
        Orient.instance().shutdown();
        thirdPartyHooks.forEach(ServerBootstrap::run);
        LOGGER.info("Shutdown ended");
    }

    private static void run(Thread t) {
        try {
            t.start();
            t.join();
        } catch (Throwable ignored) {
            // ignored anyway
        }
    }
}

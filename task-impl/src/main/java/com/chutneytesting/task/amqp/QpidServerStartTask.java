package com.chutneytesting.task.amqp;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.qpid.server.SystemLauncher;
import org.springframework.core.io.ClassPathResource;

public class QpidServerStartTask implements Task {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String initialConfiguration;

    public QpidServerStartTask(Logger logger,
                               FinallyActionRegistry finallyActionRegistry,
                               @Input("init-config") String initialConfiguration) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.initialConfiguration = Optional.ofNullable(initialConfiguration)
            .orElseGet(this::defaultConfiguration);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            SystemLauncher systemLauncher = new SystemLauncher();
            logger.info("Try to start qpid server");
            systemLauncher.startup(createSystemConfig());
            createQuitFinallyAction(systemLauncher);
            return TaskExecutionResult.ok(toOutputs(systemLauncher));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private String defaultConfiguration() {
        try {
            return new ClassPathResource("com/chutneytesting/task/amqp/default_qpid.json").getURL().toExternalForm();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private Map<String, Object> createSystemConfig() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfiguration);
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }

    private Map<String, Object> toOutputs(SystemLauncher systemLauncher) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("qpidLauncher", systemLauncher);
        return outputs;
    }

    private void createQuitFinallyAction(SystemLauncher systemLauncher) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("qpid-server-stop")
                .withInput("qpid-launcher", systemLauncher)
                .build()
        );
        logger.info("QpidServerStop finally action registered");
    }

}

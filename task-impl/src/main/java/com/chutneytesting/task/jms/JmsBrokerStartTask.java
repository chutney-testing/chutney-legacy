package com.chutneytesting.task.jms;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class JmsBrokerStartTask implements Task {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String configurationUri;

    public JmsBrokerStartTask(Logger logger,
                              FinallyActionRegistry finallyActionRegistry,
                              @Input("config-uri") String configUri) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.configurationUri = Optional.ofNullable(configUri)
            .orElseGet(this::defaultConfiguration);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            BrokerService brokerService = BrokerFactory.createBroker(configurationUri);
            logger.info("Try to start jms broker");
            brokerService.start();
            createQuitFinallyAction(brokerService);
            return TaskExecutionResult.ok(toOutputs(brokerService));
        } catch (Exception e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private String defaultConfiguration() {
        return "broker:(tcp://localhost:61616)?useJmx=false&persistent=false";
    }

    private Map<String, Object> toOutputs(BrokerService brokerService) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("jmsBrokerService", brokerService);
        return outputs;
    }

    private void createQuitFinallyAction(BrokerService brokerService) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("jms-broker-stop")
                .withInput("jms-broker-service", brokerService)
                .build()
        );
        logger.info("JmsBrokerStop finally action registered");
    }

}

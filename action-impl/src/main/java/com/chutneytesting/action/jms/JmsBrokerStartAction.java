package com.chutneytesting.action.jms;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;

public class JmsBrokerStartAction implements Action {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final String configurationUri;

    public JmsBrokerStartAction(Logger logger,
                                FinallyActionRegistry finallyActionRegistry,
                                @Input("config-uri") String configUri) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.configurationUri = Optional.ofNullable(configUri)
            .orElseGet(this::defaultConfiguration);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            ActiveMQServer brokerService = ActiveMQServers.newActiveMQServer(new ConfigurationImpl()
                .setPersistenceEnabled(false)
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("broker", this.configurationUri));
            brokerService.start();
            logger.info("Started with configuration uri: " + this.configurationUri);
            createQuitFinallyAction(brokerService);
            return ActionExecutionResult.ok(toOutputs(brokerService));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private String defaultConfiguration() {
        return "tcp://localhost:61616";
    }

    private Map<String, Object> toOutputs(ActiveMQServer brokerService) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("jmsBrokerService", brokerService);
        return outputs;
    }

    private void createQuitFinallyAction(ActiveMQServer brokerService) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("jms-broker-stop", JmsBrokerStartAction.class)
                .withInput("jms-broker-service", brokerService)
                .build()
        );
        logger.info("JmsBrokerStop finally action registered");
    }
}

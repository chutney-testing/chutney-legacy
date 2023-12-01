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

package com.chutneytesting.action.jms;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

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
            BrokerService brokerService = BrokerFactory.createBroker(configurationUri);
            logger.info("Try to start jms broker");
            brokerService.start();
            createQuitFinallyAction(brokerService);
            return ActionExecutionResult.ok(toOutputs(brokerService));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
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
                .forAction("jms-broker-stop", JmsBrokerStartAction.class)
                .withInput("jms-broker-service", brokerService)
                .build()
        );
        logger.info("JmsBrokerStop finally action registered");
    }

}

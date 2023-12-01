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

package com.chutneytesting.action.kafka;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class KafkaBrokerStartAction implements Action {

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final int port;
    private final List<String> topics;
    private final Map<String, String> properties;

    public KafkaBrokerStartAction(Logger logger,
                                FinallyActionRegistry finallyActionRegistry,
                                @Input("port") String port,
                                @Input("topics") List<String> topics,
                                @Input("properties") Map<String, String> properties) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.port = ofNullable(port).map(Integer::parseInt).orElse(-1);
        this.topics = ofNullable(topics).orElse(emptyList());
        this.properties = ofNullable(properties).orElse(emptyMap());
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            EmbeddedKafkaBroker broker = new EmbeddedKafkaBroker(1, true, topics.toArray(new String[0]));
            configure(broker);
            logger.info("Try to start kafka broker");
            broker.afterPropertiesSet();
            createQuitFinallyAction(broker);
            return ActionExecutionResult.ok(toOutputs(broker));
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private void configure(EmbeddedKafkaBroker broker) {
        if (port > 0) {
            broker.kafkaPorts(port);
        }
        if (!properties.isEmpty()) {
            broker.brokerProperties(properties);
        }
    }

    private Map<String, Object> toOutputs(EmbeddedKafkaBroker broker) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("kafkaBroker", broker);
        return outputs;
    }

    private void createQuitFinallyAction(EmbeddedKafkaBroker broker) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("kafka-broker-stop", KafkaBrokerStartAction.class)
                .withInput("broker", broker)
                .build()
        );
        logger.info("KafkaBrokerStop finally action registered");
    }
}

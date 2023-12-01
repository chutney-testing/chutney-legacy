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

package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.stream.Collectors.joining;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class AmqpBasicPublishAction implements Action {

    private static final String CONTENT_TYPE = "content_type";

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String exchangeName;
    private final String routingKey;
    private final Map<String, Object> headers;
    private final Map<String, String> properties;
    private final String payload;
    private final Logger logger;

    public AmqpBasicPublishAction(Target target,
                                @Input("exchange-name") String exchangeName,
                                @Input("routing-key") String routingKey,
                                @Input("headers") Map<String, Object> headers,
                                @Input("properties") Map<String, String> properties,
                                @Input("payload") String payload,
                                Logger logger) {
        this.target = target;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.headers = headers != null ? headers : Collections.emptyMap();
        this.properties = properties != null ? properties : Collections.emptyMap();
        this.payload = payload;
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(exchangeName, "exchange-name"),
            notBlankStringValidation(payload, "payload"),
            targetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.newConnection(target);
             Channel channel = connection.createChannel()) {

            BasicProperties basicProperties = buildProperties();
            channel.basicPublish(exchangeName, routingKey, basicProperties, payload.getBytes());
            logger.info("Published AMQP Message on " + exchangeName + " with routing key: " + routingKey);
            return ActionExecutionResult.ok(outputs(basicProperties, payload));
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }

    public Map<String, Object> outputs(BasicProperties basicProperties, String payload) {
        Map<String, Object> results = new HashMap<>();
        results.put("payload", payload);
        results.put("headers", basicProperties.getHeaders().entrySet().stream()
            .map(Map.Entry::toString)
            .collect(joining(";", "[", "]"))
        );
        return results;
    }

    private BasicProperties buildProperties() {
        Builder basicPropertiesBuilder = new Builder().appId("testing-app");
        if (properties.containsKey(CONTENT_TYPE)) {
            basicPropertiesBuilder.contentType(properties.get(CONTENT_TYPE));
        }
        basicPropertiesBuilder.headers(headers);
        return basicPropertiesBuilder.build();
    }
}

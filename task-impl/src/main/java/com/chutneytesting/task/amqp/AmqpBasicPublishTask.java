package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.TaskValidatorsUtils.stringValidation;
import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.stream.Collectors.joining;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
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

public class AmqpBasicPublishTask implements Task {

    private static final String CONTENT_TYPE = "content_type";

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String exchangeName;
    private final String routingKey;
    private final Map<String, Object> headers;
    private final Map<String, String> properties;
    private final String payload;
    private final Logger logger;

    public AmqpBasicPublishTask(Target target,
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
            stringValidation(exchangeName, "exchange-name"),
            stringValidation(payload, "payload"),
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.create(target).newConnection();
             Channel channel = connection.createChannel()) {

            BasicProperties basicProperties = buildProperties();
            channel.basicPublish(exchangeName, routingKey, basicProperties, payload.getBytes());
            logger.info("Published AMQP Message on " + exchangeName + " with routing key: " + routingKey);
            return TaskExecutionResult.ok(outputs(basicProperties, payload));
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
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

package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.amqp.utils.AmqpUtils.convertMapLongStringToString;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class AmqpBasicGetAction implements Action {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String queueName;
    private final Logger logger;

    public AmqpBasicGetAction(Target target,
                            @Input("queue-name") String queueName,
                            Logger logger) {
        this.target = target;
        this.queueName = queueName;
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(queueName, "queue-name"),
            targetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.newConnection(target);
             Channel channel = connection.createChannel()) {

            GetResponse getResponse = channel.basicGet(queueName, true);

            if (getResponse == null) {
                logger.error("No message available");
                return ActionExecutionResult.ko();
            }

            logger.info("Got AMQP Message on " + queueName + " with deliveryTag: " + getResponse.getEnvelope().getDeliveryTag());

            Map<String, Object> results = new HashMap<>();
            results.put("message", getResponse);
            results.put("body", new String(getResponse.getBody()));
            results.put("headers", convertMapLongStringToString(getResponse.getProps().getHeaders()));
            return ActionExecutionResult.ok(results);
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}

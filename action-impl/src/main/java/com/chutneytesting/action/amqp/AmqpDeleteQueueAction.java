package com.chutneytesting.action.amqp;

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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AmqpDeleteQueueAction implements Action {
    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String queueName;
    private final Logger logger;

    public AmqpDeleteQueueAction(Target target,
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

            long messageCount = channel.messageCount(queueName);
            long consumerCount = channel.consumerCount(queueName);
            channel.queueDelete(queueName);
            logger.info("Deleted AMQP Queue with name: " + queueName + " (" + messageCount + " messages, " + consumerCount + " consumers)");
            return ActionExecutionResult.ok();
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }
}

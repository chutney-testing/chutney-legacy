package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AmqpUnbindQueueTask implements Task {
    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String queueName;
    private final String exchangeName;
    private final String routingKey;
    private final Logger logger;

    public AmqpUnbindQueueTask(Target target,
                               @Input("queue-name") String queueName,
                               @Input("exchange-name") String exchangeName,
                               @Input("routing-key") String routingKey,
                               Logger logger) {
        this.target = target;
        this.queueName = queueName;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
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
    public TaskExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.newConnection(target);
             Channel channel = connection.createChannel()) {

            channel.queueUnbind(queueName, exchangeName, routingKey);
            logger.info("Deleted AMQP binding " + exchangeName + " (with " + routingKey + ") -> " + queueName);
            return TaskExecutionResult.ok();
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}

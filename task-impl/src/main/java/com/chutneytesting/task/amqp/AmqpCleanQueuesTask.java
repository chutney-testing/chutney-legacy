package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class AmqpCleanQueuesTask implements Task {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final List<String> queueNames;
    private final Logger logger;

    public AmqpCleanQueuesTask(Target target,
                               @Input("queue-names") List<String> queueNames,
                               Logger logger) {
        this.target = target;
        this.queueNames = ofNullable(queueNames).orElse(emptyList());
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.create(target).newConnection();
             Channel channel = connection.createChannel()) {
            for (String queueName : queueNames) {
                PurgeOk purgeOk = channel.queuePurge(queueName);
                logger.info("Purge queue " + queueName + ". " + purgeOk.getMessageCount() + " messages deleted");
            }
            return TaskExecutionResult.ok();
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}

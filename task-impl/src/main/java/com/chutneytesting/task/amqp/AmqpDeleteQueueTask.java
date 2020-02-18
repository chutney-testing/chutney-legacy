package com.chutneytesting.task.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AmqpDeleteQueueTask implements Task {
    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final Logger logger;

    public AmqpDeleteQueueTask(Target target,
                               @Input("queue-name") String queueName,
                               Logger logger) {
        this.connectionFactory = connectionFactoryFactory.create(target);
        this.queueName = queueName;
        this.logger = logger;
    }

    @Override
    public TaskExecutionResult execute() {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            long messageCount = channel.messageCount(queueName);
            long consumerCount = channel.consumerCount(queueName);
            channel.queueDelete(queueName);
            logger.info("Deleted AMQP Queue with name: " + queueName + " (" + messageCount + " messages, " + consumerCount + " consumers)");
            return TaskExecutionResult.ok();
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}

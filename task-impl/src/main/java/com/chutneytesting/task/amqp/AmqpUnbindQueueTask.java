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

public class AmqpUnbindQueueTask implements Task {
    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final String exchangeName;
    private final String routingKey;
    private final Logger logger;

    public AmqpUnbindQueueTask(Target target,
                               @Input("queue-name") String queueName,
                               @Input("exchange-name") String exchangeName,
                               @Input("routing-key") String routingKey,
                               Logger logger) {
        this.connectionFactory = connectionFactoryFactory.create(target);
        this.queueName = queueName;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.logger = logger;
    }

    @Override
    public TaskExecutionResult execute() {
        try (Connection connection = connectionFactory.newConnection();
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

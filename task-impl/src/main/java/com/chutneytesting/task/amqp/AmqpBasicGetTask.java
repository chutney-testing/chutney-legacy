package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.amqp.utils.AmqpUtils.convertMapLongStringToString;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class AmqpBasicGetTask implements Task {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final Logger logger;

    public AmqpBasicGetTask(Target target,
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

            GetResponse getResponse = channel.basicGet(queueName, true);

            if (getResponse == null) {
                logger.error("No message available");
                return TaskExecutionResult.ko();
            }

            logger.info("Got AMQP Message on " + queueName + " with deliveryTag: " + getResponse.getEnvelope().getDeliveryTag());

            Map<String, Object> results = new HashMap<>();
            results.put("message", getResponse);
            results.put("body", new String(getResponse.getBody()));
            results.put("headers", convertMapLongStringToString(getResponse.getProps().getHeaders()));
            return TaskExecutionResult.ok(results);
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}

package com.chutneytesting.task.amqp;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.durationValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.task.amqp.consumer.ConsumerSupervisor;
import com.chutneytesting.task.amqp.consumer.QueueingConsumer;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.tuple.Pair;

public class AmqpBasicConsumeTask implements Task {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String queueName;
    private final Integer nbMessages;
    private final String selector;
    private final String timeout;
    private final Boolean ack;
    private final Logger logger;

    public AmqpBasicConsumeTask(Target target,
                                @Input("queue-name") String queueName,
                                @Input("nb-messages") Integer nbMessages,
                                @Input("selector") String selector,
                                @Input("timeout") String timeout,
                                @Input("ack") Boolean ack,
                                Logger logger) {
        this.target = target;
        this.queueName = queueName;
        this.logger = logger;
        this.nbMessages = defaultIfNull(nbMessages, 1);
        this.timeout = defaultIfEmpty(timeout, "10 sec");
        this.selector = selector;
        this.ack = defaultIfNull(ack, true);
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(queueName, "queue-name"),
            targetValidation(target),
            durationValidation(this.timeout, "timeout")
        );
    }

    @Override
    public TaskExecutionResult execute() {
        long originalDuration = Duration.parse(timeout).toMilliseconds();

        ConsumerSupervisor instance = ConsumerSupervisor.getInstance();
        Connection connection = null;
        Channel channel = null;
        try {
            Pair<Boolean, Long> waitingResult = instance.waitUntilQueueAvailable(queueName, originalDuration, logger);
            boolean lockAcquired = waitingResult.getLeft();
            if (!lockAcquired) {
                return TaskExecutionResult.ko();
            }

            connection = connectionFactoryFactory.newConnection(target);
            channel = connection.createChannel();

            long consumingDuration = waitingResult.getRight();
            QueueingConsumer.Result result = new QueueingConsumer(channel, queueName, nbMessages, selector, consumingDuration, ack).consume();
            if (result.messages.size() != nbMessages) {
                logger.error("Unable to get the expected number of messages [" + nbMessages + "] during " + timeout + ".");
                return TaskExecutionResult.ko();
            }
            logger.info("Message(s) found in " + result.consumeDuration);
            return TaskExecutionResult.ok(extractOutputs(result));
        } catch (TimeoutException | InterruptedException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return TaskExecutionResult.ko();
        } finally {
            try {
                closeChannel(channel);
                closeConnection(connection);
            } finally {
                instance.unlock(this.queueName);
            }
        }
    }

    private Map<String, Object> extractOutputs(QueueingConsumer.Result result) {
        final Map<String, Object> results = new HashMap<>();
        results.put("body", result.messages);
        results.put("payloads", result.payloads);
        results.put("headers", result.headers);
        return results;
    }

    private void closeConnection(Connection connection) {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close(1000);
            } catch (IOException e) {
                logger.error("Error during connection closing: " + e.getMessage());
            }
        }
    }

    private void closeChannel(Channel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                logger.error("Error during channel closing: " + e.getMessage());
            }
        }
    }
}

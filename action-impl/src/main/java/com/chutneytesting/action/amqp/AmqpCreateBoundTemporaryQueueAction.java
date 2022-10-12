package com.chutneytesting.action.amqp;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class AmqpCreateBoundTemporaryQueueAction implements Action {

    private final ConnectionFactoryFactory connectionFactoryFactory = new ConnectionFactoryFactory();

    private final Target target;
    private final String exchangeName;
    private final String routingKey;
    private final String queueName;
    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;

    public AmqpCreateBoundTemporaryQueueAction(Target target,
                                             @Input("exchange-name") String exchangeName,
                                             @Input("routing-key") String routingKey,
                                             @Input("queue-name") String queueName,
                                             Logger logger,
                                             FinallyActionRegistry finallyActionRegistry) {
        this.target = target;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queueName = queueName;
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(queueName, "queue-name"),
            notBlankStringValidation(queueName, "exchange-name"),
            targetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (Connection connection = connectionFactoryFactory.newConnection(target);
             Channel channel = connection.createChannel()) {
            createQueue(queueName, channel);
            bindQueue(channel, queueName);
            createQuitFinallyActions();
            return ActionExecutionResult.ok(Collections.singletonMap("queueName", queueName));
        } catch (TimeoutException | IOException e) {
            logger.error("Unable to establish connection to RabbitMQ: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }

    private void bindQueue(Channel channel, String queueName) throws IOException {
        String routingKey = Optional.ofNullable(this.routingKey).orElse(queueName);
        channel.queueBind(queueName, exchangeName, routingKey);
        logger.info("Created AMQP binding " + exchangeName + " (with " + this.routingKey + ") -> " + queueName);
    }

    private void createQueue(String queueName, Channel channel) throws IOException {
        channel.queueDeclare(queueName, true, false, false, null);
        logger.info("Created AMQP Queue with name: " + queueName);
    }

    private void createQuitFinallyActions() {
        finallyActionRegistry.registerFinallyAction(FinallyAction.Builder
            .forAction("amqp-unbind-queue", AmqpCreateBoundTemporaryQueueAction.class)
            .withTarget(target)
            .withInput("queue-name", queueName)
            .withInput("exchange-name", exchangeName)
            .withInput("routing-key", routingKey)
            .build());
        logger.info("Registered unbinding queue finally action");

        finallyActionRegistry.registerFinallyAction(FinallyAction.Builder
            .forAction("amqp-delete-queue", AmqpCreateBoundTemporaryQueueAction.class.getSimpleName())
            .withTarget(target)
            .withInput("queue-name", queueName)
            .build());
        logger.info("Registered delete queue finally action");
    }
}

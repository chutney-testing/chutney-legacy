package com.chutneytesting.action.jakarta;

import static com.chutneytesting.action.jakarta.JakartaActionParameter.DESTINATION;
import static com.chutneytesting.action.jakarta.JakartaActionParameter.TIMEOUT;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.action.jakarta.consumer.Consumer;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JakartaCleanQueueAction implements Action {

    private final Target target;
    private final Logger logger;

    private final JakartaConnectionFactory jmsConnectionFactory = new JakartaConnectionFactory();
    private final String destination;
    private final String timeout;

    public JakartaCleanQueueAction(Target target, Logger logger, @Input(DESTINATION) String destination, @Input(TIMEOUT) String timeout) {
        this.target = target;
        this.logger = logger;
        this.destination = destination;
        this.timeout = defaultIfEmpty(timeout, "500 ms");
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(destination, "destination"),
            durationValidation(timeout, "timeout"),
            targetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {

        try (CloseableResource<Consumer> consumer = jmsConnectionFactory.createConsumer(target, destination, timeout)) {
            int removedMessages = 0;
            Optional<Message> message;
            while ((message = consumer.getResource().getMessage()).isPresent()) {
                displayMessageContent(logger, message.get());
                removedMessages++;
            }

            logger.info("Removed " + removedMessages + " messages");
            return ActionExecutionResult.ok();
        } catch (JMSException | UncheckedJakartaException e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }

    private void displayMessageContent(Logger logger, Message message) throws JMSException {
        Map<String, String> properties = propertiesToMap(message);

        final String body;
        if (message instanceof TextMessage textMessage) {
            body = textMessage.getText();
        } else {
            body = "";
        }
        logger.info("Removed: " + properties + " " + body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> propertiesToMap(Message message) throws JMSException {
        Enumeration<String> propertyNames = message.getPropertyNames();
        Map<String, String> properties = new LinkedHashMap<>();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            properties.put(propertyName, String.valueOf(message.getObjectProperty(propertyName)));
        }
        return properties;
    }
}

package com.chutneytesting.action.jms;

import static com.chutneytesting.action.jms.JmsActionParameter.DESTINATION;
import static com.chutneytesting.action.jms.JmsActionParameter.TIMEOUT;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.action.jms.consumer.Consumer;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JmsListenerAction implements Action {

    private final Target target;
    private final Logger logger;

    private final String destination;
    private final String timeout;
    private final JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
    private final String bodySelector;
    private final String selector;
    private final int browserMaxDepth;

    public JmsListenerAction(Target target, Logger logger, @Input(DESTINATION) String destination, @Input(TIMEOUT) String timeout, @Input("bodySelector") String bodySelector, @Input("selector") String selector, @Input("browserMaxDepth") Integer browserMaxDepth) {
        this.target = target;
        this.logger = logger;
        this.destination = destination;
        this.timeout = defaultIfEmpty(timeout, "500 ms");
        this.bodySelector = bodySelector;
        this.selector = selector;
        this.browserMaxDepth = defaultIfNull(browserMaxDepth, 30);

        if (browserMaxDepth != null && bodySelector == null) {
            logger.error("[WARNING] browserMaxDepth is only used if bodySelector is filled");
        }
        if (bodySelector != null && timeout != null) {
            logger.error("[WARNING] timeout is only used if bodySelector is NOT filled");
        }
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
        try (CloseableResource<Consumer> consumerCloseableResource = jmsConnectionFactory.createConsumer(target, destination, timeout, bodySelector, selector, browserMaxDepth)) {
            Optional<Message> matchingMessage = consumerCloseableResource.getResource().getMessage();
            if (matchingMessage.isPresent()) {
                Message message = matchingMessage.get();
                if (message instanceof TextMessage textMessage) {
                    logger.info("Jms message received: " + textMessage.getText());
                    return ActionExecutionResult.ok(toOutputs(textMessage));
                } else {
                    logger.error("JMS message type not handled: " + message.getClass().getSimpleName());
                }
            } else {
                logger.error("No message available");
            }
        } catch (JMSException | UncheckedJmsException | IllegalArgumentException | IllegalStateException e) {
            logger.error(e);
        }
        return ActionExecutionResult.ko();
    }

    private static Map<String, Object> toOutputs(TextMessage message) throws JMSException {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("textMessage", message.getText());
        outputs.put("jmsProperties", getMessageProperties(message));
        return outputs;
    }

    private static HashMap<String, Object> getMessageProperties(Message msg) throws JMSException {
        HashMap<String, Object> properties = new HashMap<>();
        Enumeration srcProperties = msg.getPropertyNames();
        while (srcProperties.hasMoreElements()) {
            String propertyName = (String) srcProperties.nextElement();
            properties.put(propertyName, msg.getObjectProperty(propertyName));
        }
        return properties;
    }
}

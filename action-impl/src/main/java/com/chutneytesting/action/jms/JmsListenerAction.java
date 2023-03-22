package com.chutneytesting.action.jms;

import static com.chutneytesting.action.jms.consumer.JmsListenerParameters.validateJmsListenerParameters;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.action.jms.consumer.Consumer;
import com.chutneytesting.action.jms.consumer.JmsListenerParameters;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.commons.lang3.ArrayUtils;

public class JmsListenerAction implements Action {

    private final Target target;
    private final Logger logger;

    private final JmsListenerParameters listenerJmsParameters;

    private final JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();

    public JmsListenerAction(Target target, Logger logger, @Input("listenerJmsParameters") JmsListenerParameters listenerJmsParameters) {
        this.target = target;
        this.logger = logger;
        this.listenerJmsParameters = listenerJmsParameters;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            ArrayUtils.add(
                validateJmsListenerParameters(listenerJmsParameters),
                targetValidation(target))
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (CloseableResource<Consumer> consumerCloseableResource = jmsConnectionFactory.createConsumer(target, listenerJmsParameters)) {
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

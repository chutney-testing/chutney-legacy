package com.chutneytesting.task.jms;

import static com.chutneytesting.task.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.jms.consumer.JmsListenerParameters.validateJmsListenerParameters;

import com.chutneytesting.task.jms.consumer.Consumer;
import com.chutneytesting.task.jms.consumer.JmsListenerParameters;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class JmsListenerTask implements Task {

    private final Target target;
    private final Logger logger;

    private final JmsListenerParameters listenerJmsParameters;

    private final JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();

    public JmsListenerTask(Target target, Logger logger, @Input("listenerJmsParameters") JmsListenerParameters listenerJmsParameters) {
        this.target = target;
        this.logger = logger;
        this.listenerJmsParameters = listenerJmsParameters;
    }

    @Override
    public List<String> validateInputs() {
        List<String> errors = targetValidation(target).getErrors();
        errors.addAll(validateJmsListenerParameters(listenerJmsParameters));
        return errors;
    }

    @Override
    public TaskExecutionResult execute() {
        try (CloseableResource<Consumer> consumerCloseableResource = jmsConnectionFactory.createConsumer(target, listenerJmsParameters)) {
            Optional<Message> matchingMessage = consumerCloseableResource.getResource().getMessage();
            if (matchingMessage.isPresent()) {
                Message message = matchingMessage.get();
                if (message instanceof TextMessage) {
                    logger.info("Jms message received: " + ((TextMessage) message).getText());
                    return TaskExecutionResult.ok(toOutputs((TextMessage) message));
                } else {
                    logger.error("JMS message type not handled: " + message.getClass().getSimpleName());
                }
            } else {
                logger.error("No message available");
            }
        } catch (JMSException | UncheckedJmsException | IllegalArgumentException | IllegalStateException e) {
            logger.error(e);
        }
        return TaskExecutionResult.ko();
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

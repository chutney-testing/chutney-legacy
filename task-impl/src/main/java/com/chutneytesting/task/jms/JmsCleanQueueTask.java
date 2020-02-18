package com.chutneytesting.task.jms;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.jms.consumer.Consumer;
import com.chutneytesting.task.jms.consumer.JmsListenerParameters;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import com.chutneytesting.tools.CloseableResource;

public class JmsCleanQueueTask implements Task {

    private final Target target;
    private final Logger logger;

    private final JmsListenerParameters listenerJmsParameters;

    private JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();

    public JmsCleanQueueTask(Target target, Logger logger, @Input("listenerJmsParameters") JmsListenerParameters listenerJmsParameters) {
        this.target = target;
        this.logger = logger;
        this.listenerJmsParameters = listenerJmsParameters;
    }

    @Override
    public TaskExecutionResult execute() {

        try (CloseableResource<Consumer> consumer = jmsConnectionFactory.createConsumer(target, listenerJmsParameters)) {
            int removedMessages = 0;
            Optional<Message> message;
            while ((message = consumer.getResource().getMessage()).isPresent()) {
                displayMessageContent(logger, message.get());
                removedMessages++;
            }

            logger.info("Removed " + removedMessages + " messages");
            return TaskExecutionResult.ok();
        } catch (JMSException | UncheckedJmsException e) {
            logger.error(e);
            return TaskExecutionResult.ko();
        }
    }

    private void displayMessageContent(Logger logger, Message message) throws JMSException {
        Map<String, String> properties = propertiesToMap(message);

        final String body;
        if (message instanceof TextMessage) {
            body = ((TextMessage) message).getText();
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
            String propertyName = (String) propertyNames.nextElement();
            properties.put(propertyName, String.valueOf(message.getObjectProperty(propertyName)));
        }
        return properties;
    }
}

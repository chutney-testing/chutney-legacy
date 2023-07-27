package com.chutneytesting.action.jms.consumer.bodySelector;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TextMessageBodySelector implements BodySelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextMessageBodySelector.class);

    @Override
    public boolean match(Message message) {
        final boolean matches;
        if (message instanceof TextMessage textMessage) {
            Optional<String> messageBody = textContent(textMessage);
            matches = messageBody.map(this::match).orElse(Boolean.FALSE);
        } else {
            matches = false;
        }
        return matches;
    }

    public abstract boolean match(String messageBody);

    private Optional<String> textContent(TextMessage message) {
        try {
            String messageBody = message.getText();
            return Optional.ofNullable(messageBody);
        } catch (JMSException e) {
            LOGGER.warn("Unable to read text from JMS TextMessage", e);
        }
        return Optional.empty();
    }
}

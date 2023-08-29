package com.chutneytesting.action.jakarta.consumer;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import java.util.Optional;

class SimpleMessageConsumer implements Consumer {

    private final MessageConsumer messageConsumer;
    private final long timeout;

    SimpleMessageConsumer(MessageConsumer messageConsumer, long timeout) {
        this.messageConsumer = messageConsumer;
        this.timeout = timeout;
    }

    @Override
    public Optional<Message> getMessage() throws JMSException {
        return Optional.ofNullable(messageConsumer.receive(timeout));
    }
}

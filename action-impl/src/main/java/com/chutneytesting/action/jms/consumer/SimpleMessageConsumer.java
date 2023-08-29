package com.chutneytesting.action.jms.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
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

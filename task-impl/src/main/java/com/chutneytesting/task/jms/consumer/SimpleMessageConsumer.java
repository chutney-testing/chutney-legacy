package com.chutneytesting.task.jms.consumer;

import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

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

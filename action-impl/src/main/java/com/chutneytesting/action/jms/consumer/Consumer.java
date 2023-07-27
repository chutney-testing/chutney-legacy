package com.chutneytesting.action.jms.consumer;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Optional;

public interface Consumer {

    Optional<Message> getMessage() throws JMSException;
}

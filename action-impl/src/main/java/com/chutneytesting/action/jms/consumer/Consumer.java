package com.chutneytesting.action.jms.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Optional;

public interface Consumer {

    Optional<Message> getMessage() throws JMSException;
}

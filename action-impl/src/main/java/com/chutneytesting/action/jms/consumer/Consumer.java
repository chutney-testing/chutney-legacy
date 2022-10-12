package com.chutneytesting.action.jms.consumer;

import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;

public interface Consumer {

    Optional<Message> getMessage() throws JMSException;
}

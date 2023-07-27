package com.chutneytesting.action.jms.consumer.bodySelector;

import jakarta.jms.Message;

public interface BodySelector {

    boolean match(Message message);
}

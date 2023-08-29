package com.chutneytesting.action.jakarta.consumer.bodySelector;

import jakarta.jms.Message;

public interface BodySelector {

    boolean match(Message message);
}

package com.chutneytesting.action.jms.consumer.bodySelector;

import javax.jms.Message;

public interface BodySelector {

    boolean match(Message message);
}

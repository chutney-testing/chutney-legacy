package com.chutneytesting.task.jms.consumer.bodySelector;

import javax.jms.Message;

public interface BodySelector {

    boolean match(Message message);
}

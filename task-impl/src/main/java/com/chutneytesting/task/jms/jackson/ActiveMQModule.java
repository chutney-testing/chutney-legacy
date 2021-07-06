package com.chutneytesting.task.jms.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.activemq.broker.BrokerService;

public class ActiveMQModule extends SimpleModule {

    private static final String NAME = "ChutneyActiveMQModule";

    public ActiveMQModule() {
        super(NAME);
        addSerializer(BrokerService.class, new BrokerServiceSerializer());
    }
}

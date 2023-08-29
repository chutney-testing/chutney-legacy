package com.chutneytesting.action.jms.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.activemq.broker.BrokerService;

public class BrokerMQModule extends SimpleModule {

    private static final String NAME = "ChutneyBrokerMQModule ";

    public BrokerMQModule() {
        super(NAME);
        addSerializer(BrokerService.class, new BrokerServiceSerializer());
    }
}

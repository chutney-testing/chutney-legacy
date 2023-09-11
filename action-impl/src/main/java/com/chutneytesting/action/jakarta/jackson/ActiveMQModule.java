package com.chutneytesting.action.jakarta.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.activemq.artemis.core.server.ActiveMQServer;

public class ActiveMQModule extends SimpleModule {

    private static final String NAME = "ChutneyActiveMQModule";

    public ActiveMQModule() {
        super(NAME);
        addSerializer(ActiveMQServer.class, new ActiveMQServerSerializer());
    }
}

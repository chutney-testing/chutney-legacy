package com.chutneytesting.action.jakarta.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.apache.activemq.artemis.core.server.ActiveMQServer;

public class ActiveMQServerSerializer extends StdSerializer<ActiveMQServer> {

    protected ActiveMQServerSerializer() {
        super(ActiveMQServer.class);
    }

    @Override
    public void serialize(ActiveMQServer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("ActiveMQ Broker Service", value.toString());
        gen.writeEndObject();
    }
}


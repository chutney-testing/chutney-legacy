package com.chutneytesting.task.jms.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.apache.activemq.broker.BrokerService;

public class BrokerServiceSerializer extends StdSerializer<BrokerService> {

    protected BrokerServiceSerializer() {
        super(BrokerService.class);
    }

    @Override
    public void serialize(BrokerService value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("ActiveMQ Broker Service", value.toString());
        gen.writeEndObject();
    }
}

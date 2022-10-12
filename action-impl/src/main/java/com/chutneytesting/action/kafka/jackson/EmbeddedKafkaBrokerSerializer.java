package com.chutneytesting.action.kafka.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

public class EmbeddedKafkaBrokerSerializer extends StdSerializer<EmbeddedKafkaBroker> {

    protected EmbeddedKafkaBrokerSerializer() {
        super(EmbeddedKafkaBroker.class);
    }

    @Override
    public void serialize(EmbeddedKafkaBroker value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("Embedded Kafka Broker", value.getBrokersAsString());
        gen.writeEndObject();
    }
}

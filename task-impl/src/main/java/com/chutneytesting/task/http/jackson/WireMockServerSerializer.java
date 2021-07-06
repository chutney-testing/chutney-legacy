package com.chutneytesting.task.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;

public class WireMockServerSerializer extends StdSerializer<WireMockServer> {

    protected WireMockServerSerializer() {
        super(WireMockServer.class);
    }

    @Override
    public void serialize(WireMockServer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("https-server-instance", value.toString());
        // TODO - Eventually add some information on wiremockserver
        gen.writeEndObject();
    }
}

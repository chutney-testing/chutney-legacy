package com.chutneytesting.action.selenium.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverSerializer extends StdSerializer<WebDriver> {

    protected WebDriverSerializer() {
        super(WebDriver.class);
    }

    @Override
    public void serialize(WebDriver value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("driver", value.toString());
        if (value instanceof RemoteWebDriver driver) {
            gen.writeStringField("capabilities", driver.getCapabilities().asMap().toString());
        }
        gen.writeEndObject();
    }
}

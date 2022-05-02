package com.chutneytesting.junit.engine.jackson;

import com.chutneytesting.engine.api.execution.StatusDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Collection;

public class StepExecutionReportSerializer extends StdSerializer<StepExecutionReportDto> {

    protected StepExecutionReportSerializer() {
        super(StepExecutionReportDto.class);
    }

    @Override
    public void serialize(StepExecutionReportDto value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        serialize(value, true, gen, provider);
    }

    public void serialize(StepExecutionReportDto value, boolean isRoot, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("name", value.name);
        if (isRoot) {
            gen.writeStringField("environment", value.environment);
        }
        gen.writeStringField("status", value.status.name());

        if (!value.context.evaluatedInputs.isEmpty()) {
            gen.writeFieldName("inputs");
            gen.writeObject(value.context.evaluatedInputs);
        }

        if (!value.information.isEmpty()) {
            writeStringCollection(gen, "informations", value.information);
        }

        if (StatusDto.FAILURE.equals(value.status) && value.steps.isEmpty()) {
            writeStringCollection(gen, "errors", value.errors);
            if (!value.context.scenarioContext.isEmpty()) {
                gen.writeFieldName("context");
                gen.writeObject(value.context.scenarioContext);
            }
        }

        gen.writeArrayFieldStart("steps");
        for (StepExecutionReportDto step : value.steps) {
            serialize(step, false, gen, provider);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }

    private void writeStringCollection(JsonGenerator gen, String field, Collection<String> list) throws IOException {
        gen.writeArrayFieldStart(field);
        for (String err : list) {
            gen.writeString(err);
        }
        gen.writeEndArray();
    }
}

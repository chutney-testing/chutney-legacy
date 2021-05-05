package com.chutneytesting.task.assertion;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonValidationTask implements Task {

    private final String json;
    private final String schema;
    private final Logger logger;

    public JsonValidationTask(Logger logger, @Input("json") String json, @Input("schema") String schema) {
        this.logger = logger;
        this.json = json;
        this.schema = schema;
    }

    @Override
    public TaskExecutionResult execute() {

        try {
            final JSONObject schemaJson = new JSONObject(new JSONTokener(schema));
            final JSONObject document = new JSONObject(new JSONTokener(json));
            final Schema createdSchema = SchemaLoader.load(schemaJson);
            createdSchema.validate(document);
        } catch (ValidationException validationException) {
            validationException.getAllMessages().forEach(message -> logger.error(message));
            return TaskExecutionResult.ko();
        } catch (JSONException jsonException) {
            logger.error("Exception: " + jsonException.getMessage());
            return TaskExecutionResult.ko();
        }
        return TaskExecutionResult.ok();
    }

}

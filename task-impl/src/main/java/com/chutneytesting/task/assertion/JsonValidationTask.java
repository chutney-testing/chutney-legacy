package com.chutneytesting.task.assertion;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
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
    public List<String> validateInputs() {
        Validator<String> jsonValidation = of(json)
            .validate(Objects::nonNull, "No json provided")
            .validate(j -> new JSONObject(new JSONTokener(j)), noException -> true, "Cannot parse json");
        Validator<String> schemaValidation = of(schema)
            .validate(Objects::nonNull, "No schema provided")
            .validate(s -> new JSONObject(new JSONTokener(s)), noException -> true, "Cannot parse schema");
        return getErrorsFrom(jsonValidation, schemaValidation);
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

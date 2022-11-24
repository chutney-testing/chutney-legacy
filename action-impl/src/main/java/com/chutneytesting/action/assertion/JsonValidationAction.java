package com.chutneytesting.action.assertion;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonValidationAction implements Action {

    private final String json;
    private final String schema;
    private final Logger logger;

    public JsonValidationAction(Logger logger, @Input("json") String json, @Input("schema") String schema) {
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
    public ActionExecutionResult execute() {
        try {
            final JSONObject schemaJson = new JSONObject(new JSONTokener(schema));
            final JSONObject document = new JSONObject(new JSONTokener(json));
            final Schema createdSchema = SchemaLoader.load(schemaJson);
            createdSchema.validate(document);
        } catch (ValidationException validationException) {
            validationException.getAllMessages().forEach(message -> logger.error(message));
            return ActionExecutionResult.ko();
        } catch (JSONException jsonException) {
            logger.error("Exception: " + jsonException.getMessage());
            return ActionExecutionResult.ko();
        }
        return ActionExecutionResult.ok();
    }

}

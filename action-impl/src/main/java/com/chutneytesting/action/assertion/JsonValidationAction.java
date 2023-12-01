/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

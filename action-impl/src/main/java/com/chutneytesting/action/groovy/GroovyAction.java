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

package com.chutneytesting.action.groovy;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

public class GroovyAction implements Action {

    private final String scriptAsString;
    private final Map<String, Object> parameters;
    private final Logger logger;

    public GroovyAction(@Input("script") String scriptAsString,
                      @Input("parameters") Map<String, Object> parameters,
                      Logger logger) {
        this.scriptAsString = scriptAsString;
        this.parameters = ofNullable(parameters).orElse(emptyMap());
        this.logger = logger;
    }

    @Override
    public List<String> validateInputs() {
        Validator<String> scriptValidation = of(scriptAsString)
            .validate(Objects::nonNull, "No script provided")
            .validate(StringUtils::isNotBlank, "Script is empty");
        return getErrorsFrom(scriptValidation);
    }

    @Override
    public ActionExecutionResult execute() {
        try {
            Script script = new GroovyShell().parse(scriptAsString);
            script.setBinding(getBindingFromMap(parameters));

            Map<String, Object> result = (Map<String, Object>) script.run();

            return ActionExecutionResult.ok(result);
        } catch (CompilationFailedException e) {
            logger.error("Cannot compile groovy script : " + e.getMessage());
            return ActionExecutionResult.ko();
        } catch (RuntimeException e) {
            logger.error("Groovy script failed during execution: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }

    private Binding getBindingFromMap(Map<String, Object> variables) {
        Binding binding = new Binding();
        variables.forEach((k, v) -> binding.setVariable(k, v));
        binding.setVariable("logger", logger);
        return binding;
    }
}

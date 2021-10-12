package com.chutneytesting.task.groovy;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;

public class GroovyTask implements Task {

    private final String scriptAsString;
    private final Map<String, Object> parameters;
    private final Logger logger;

    public GroovyTask(@Input("script") String scriptAsString,
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
    public TaskExecutionResult execute() {
        try {
            Script script = new GroovyShell().parse(scriptAsString);
            script.setBinding(getBindingFromMap(parameters));

            Map<String, Object> result = (Map<String, Object>) script.run();

            return TaskExecutionResult.ok(result);
        } catch (CompilationFailedException e) {
            logger.error("Cannot compile groovy script : " + e.getMessage());
            return TaskExecutionResult.ko();
        } catch (RuntimeException e) {
            logger.error("Groovy script failed during execution: " + e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

    private Binding getBindingFromMap(Map<String, Object> variables) {
        Binding binding = new Binding();
        variables.forEach((k, v) -> binding.setVariable(k, v));
        binding.setVariable("logger", logger);
        return binding;
    }
}

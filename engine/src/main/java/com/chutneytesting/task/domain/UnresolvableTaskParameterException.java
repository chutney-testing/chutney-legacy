package com.chutneytesting.task.domain;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import java.util.List;

/**
 * Thrown when a {@link TaskTemplate} fails to create a task
 * because a {@link Parameter} does not have a matching {@link ParameterResolver}.
 *
 * @see TaskTemplate#create(List)
 */
public class UnresolvableTaskParameterException extends RuntimeException {

    private final String taskIdentifier;
    private final Parameter unresolvableParameter;

    public UnresolvableTaskParameterException(String taskIdentifier, Parameter unresolvableParameter) {
        super("Unable to resolve " + unresolvableParameter + " of Task[" + taskIdentifier + "]");

        this.taskIdentifier = taskIdentifier;
        this.unresolvableParameter = unresolvableParameter;
    }

    public String taskIdentifier() {
        return taskIdentifier;
    }

    public Parameter unresolvableParameter() {
        return unresolvableParameter;
    }
}

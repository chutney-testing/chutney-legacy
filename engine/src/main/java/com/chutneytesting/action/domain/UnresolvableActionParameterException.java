package com.chutneytesting.action.domain;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import java.util.List;

/**
 * Thrown when a {@link ActionTemplate} fails to create a action
 * because a {@link Parameter} does not have a matching {@link ParameterResolver}.
 *
 * @see ActionTemplate#create(List)
 */
public class UnresolvableActionParameterException extends RuntimeException {

    private final String actionIdentifier;
    private final Parameter unresolvableParameter;

    public UnresolvableActionParameterException(String actionIdentifier, Parameter unresolvableParameter) {
        super("Unable to resolve " + unresolvableParameter + " of Action[" + actionIdentifier + "]");

        this.actionIdentifier = actionIdentifier;
        this.unresolvableParameter = unresolvableParameter;
    }

    public String actionIdentifier() {
        return actionIdentifier;
    }

    public Parameter unresolvableParameter() {
        return unresolvableParameter;
    }
}

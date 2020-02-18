package com.chutneytesting.task.domain;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.Task;
import java.util.List;
import java.util.Set;

/**
 * Template for creating {@link Task} instances.<br>
 * This object exposes all parameters (names and types) needed by a {@link Task}
 */
public interface TaskTemplate {

    /**
     * @return an identifier to link task description in a scenario and its implementation
     */
    String identifier();

    /**
     * @return the class parsed into the current {@link TaskTemplate}. May not be a {@link Task} if adaptation is made to comply to the current SPI.
     */
    Class<?> implementationClass();

    /**
     * @return {@link Parameter}s needed to create an instance of {@link Task}
     */
    Set<Parameter> parameters();

    Task create(List<ParameterResolver> parameterResolvers) throws UnresolvableTaskParameterException, TaskInstantiationFailureException;

    default <T> T resolveParameter(List<ParameterResolver> parameterResolvers, Parameter parameter) {
        return (T) parameterResolvers
            .stream()
            .filter(pr -> pr.canResolve(parameter))
            .findFirst()
            .orElseThrow(() -> new UnresolvableTaskParameterException(identifier(), parameter))
            .resolve(parameter);
    }
}

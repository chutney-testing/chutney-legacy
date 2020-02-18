package com.chutneytesting.task.domain;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.Task;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TaskTemplateV2 implements TaskTemplate {

    private final String identifier;
    private final Class<? extends Task> implementationClass;
    private final Constructor<? extends Task> constructor;
    private final List<Parameter> parameters;

    public TaskTemplateV2(String identifier, Class<? extends Task> implementationClass, Constructor<? extends Task> constructor, List<Parameter> parameters) {
        this.identifier = identifier;
        this.implementationClass = implementationClass;
        this.constructor = constructor;
        this.parameters = parameters;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public Class<? extends Task> implementationClass() {
        return implementationClass;
    }

    @Override
    public Set<Parameter> parameters() {
        return new LinkedHashSet<>(parameters);
    }

    @Override
    public Task create(List<ParameterResolver> parameterResolvers) {
        Object[] parameterValues = parameters.stream()
            .map(p -> resolveParameter(parameterResolvers, p))
            .toArray(Object[]::new);
        try {
            return constructor.newInstance(parameterValues);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new TaskInstantiationFailureException(identifier, e);
        }
    }
}

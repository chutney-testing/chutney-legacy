package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;

public class TypedValueParameterResolver<T> implements ParameterResolver {

    private final Class<? extends T> matchingType;
    private final T value;

    public TypedValueParameterResolver(Class<? extends T> matchingType, T value) {
        this.matchingType = matchingType;
        this.value = value;
    }

    @Override
    public boolean canResolve(Parameter parameter) {
        return matchingType.equals(parameter.rawType());
    }

    @Override
    public Object resolve(Parameter parameter) {
        return value;
    }
}

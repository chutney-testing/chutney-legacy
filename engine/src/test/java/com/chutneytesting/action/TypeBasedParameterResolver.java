package com.chutneytesting.action;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import java.util.function.Function;

public class TypeBasedParameterResolver<T> implements ParameterResolver {

    private final Class<?> resolvedType;
    private final Function<Parameter, T> valueProducer;

    public TypeBasedParameterResolver(Class<T> resolvedType, Function<Parameter, T> valueProducer) {
        this.resolvedType = resolvedType;
        this.valueProducer = valueProducer;
    }

    @Override
    public boolean canResolve(Parameter parameter) {
        return resolvedType.equals(parameter.rawType());
    }

    @Override
    public T resolve(Parameter parameter) {
        return valueProducer.apply(parameter);
    }
}

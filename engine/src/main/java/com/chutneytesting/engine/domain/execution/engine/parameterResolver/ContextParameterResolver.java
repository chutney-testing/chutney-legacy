package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import java.util.Map;

public class ContextParameterResolver implements ParameterResolver {

    private final Map<String, Object> inputs;

    public ContextParameterResolver(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean canResolve(Parameter parameter) {
        return parameter.annotations().isEmpty() && parameter.rawType().equals(Map.class);
    }

    @Override
    public Object resolve(Parameter parameter) {
        return inputs;
    }

}

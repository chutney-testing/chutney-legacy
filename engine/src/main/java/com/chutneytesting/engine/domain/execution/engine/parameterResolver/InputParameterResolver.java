package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.injectable.Input;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * TODO do not mix input parameter parsing and task instantiation
 */
public class InputParameterResolver implements ParameterResolver {

    private final Map<String, Object> inputs;

    public InputParameterResolver(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean canResolve(Parameter parameter) {
        return parameter.annotations().optional(Input.class).isPresent();
    }

    @Override
    public Object resolve(Parameter parameter) {
        String inputName = getValidParameter(parameter);
        Object inputValue = inputs.get(inputName);

        if (isSimpleType(parameter)) {
            // TODO ugly hack n°1
            if (inputValue == null) {
                return null;
            }
            // TODO ugly hack n°2 since it is related to parsing, it should be out of the engine
            Class<?> inputClassType = inputValue.getClass();
            if (parameter.rawType().isAssignableFrom(inputClassType)) {
                return inputValue;
            } else if (inputValue instanceof Map) {
                return new JSONObject((Map) inputValue).toString();
            } else {
                throw new IllegalArgumentException(inputName + " type is " + inputClassType + ", should be " + parameter.rawType());
            }
        } else {
            Optional<Object> valueInstantiate = createObjectFromInputs(inputs, parameter.rawType());
            return valueInstantiate.orElse(inputValue);
        }
    }

    private String getValidParameter(Parameter parameter) {
        Input input = parameter.annotations().get(Input.class);
        String inputName = input.value();
        if (StringUtils.isEmpty(inputName)) {
            throw new InputNameMandatoryException();
        }
        return inputName;
    }

    /**
     * @return true if rawtype of parameter is a type simply deserializable
     */
    private boolean isSimpleType(Parameter parameter) {
        return parameter.rawType().isPrimitive() || "java.lang".equals(parameter.rawType().getPackage().getName());
    }

    private Optional<Object> createObjectFromInputs(Map<String, Object> inputs, Class<?> aClass) {
        Constructor<?>[] constructors = aClass.getConstructors();
        if (constructors.length == 1) {
            Constructor constructor = constructors[0];
            List<Object> parameters = Arrays.stream(constructor.getParameters())
                .map(
                    p -> {
                        String inputName = getValidParameter(Parameter.fromJavaParameter(p));
                        return inputs.get(inputName);
                    })
                .collect(Collectors.toList());
            try {
                return Optional.of(constructor.newInstance(parameters.toArray()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }
}

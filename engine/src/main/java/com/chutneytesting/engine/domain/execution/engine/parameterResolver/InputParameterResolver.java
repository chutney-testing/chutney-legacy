package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import com.chutneytesting.action.spi.injectable.Input;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * TODO do not mix input parameter parsing and action instantiation
 */
public class InputParameterResolver implements ParameterResolver {

    private final Map<String, Object> inputs;
    private static final Map<Class<?>, Function<String, ?>> primitivesValueOf = new HashMap<>();

    static {
        primitivesValueOf.put(Short.class, Short::valueOf);
        primitivesValueOf.put(Integer.class, Integer::valueOf);
        primitivesValueOf.put(Long.class, Long::valueOf);
        primitivesValueOf.put(Boolean.class, Boolean::valueOf);
        //primitivesValueOf.put(Character.class, Character::valueOf);
        primitivesValueOf.put(Character.class, InputParameterResolver::characterValueOf);
        primitivesValueOf.put(Float.class, Float::valueOf);
        primitivesValueOf.put(Double.class, Double::valueOf);
        primitivesValueOf.put(Byte.class, Byte::valueOf);
    }

    public InputParameterResolver(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    @Override
    public boolean canResolve(Parameter parameter) {
        return parameter.annotations().optional(Input.class).isPresent();
    }

    @Override
    public Object resolve(Parameter parameter) {
        boolean isParameterPrimitive = isPrimitiveOrWrapper(parameter.rawType());
        String inputName = getValidParameter(parameter);
        Object inputValue = inputs.get(inputName);

        if (inputValue == null) {
            if (isParameterPrimitive) {
                return null;
            } else {
                Optional<Object> valueInstantiate = createObjectFromInputs(inputs, parameter.rawType());
                return valueInstantiate.orElse(null);
            }
        }

        Class<?> inputClassType =  inputValue.getClass();
        if (parameter.rawType().isAssignableFrom(inputClassType)) {
            return inputValue;
        }

        if (parameter.rawType().equals(String.class)) {
            if (isPrimitiveOrWrapper(inputClassType)) {
                return inputValue.toString();
            } else if (inputValue instanceof Map map) {
                // TODO ugly hack since it is related to parsing, it should be out of the engine
                return new JSONObject(map).toString();
            }
        } else if (inputClassType.equals(String.class)) {
            Object inputResolution = valueOf(parameter.rawType(), (String) inputValue);
            if (inputResolution != null) {
                return inputResolution;
            }
        }

        throw new IllegalArgumentException(inputName + " type is " + inputClassType + ", should be " + parameter.rawType());
    }

    private Object valueOf(Class<?> clazz, String inputValue) {
        return ofNullable(primitivesValueOf.get(clazz))
            .map(m -> m.apply(inputValue))
            .orElse(null);
    }

    private String getValidParameter(Parameter parameter) {
        Input input = parameter.annotations().get(Input.class);
        String inputName = input.value();
        if (StringUtils.isEmpty(inputName)) {
            throw new InputNameMandatoryException();
        }
        return inputName;
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
                .toList();
            try {
                return Optional.of(constructor.newInstance(parameters.toArray()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    private static Character characterValueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException("null");
        }

        if (s.isEmpty()) {
            throw new IllegalArgumentException("empty");
        }

        return s.charAt(0);
    }
}

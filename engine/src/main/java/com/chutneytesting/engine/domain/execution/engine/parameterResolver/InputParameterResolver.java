/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution.engine.parameterResolver;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import com.chutneytesting.action.spi.injectable.Input;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * TODO do not mix input parameter parsing and action instantiation
 * May be we can remove the hack because nobody write directly json now
 */
public class InputParameterResolver implements ParameterResolver {

    private final Map<String, Object> inputs;
    private static final Map<Class<?>, Function<String, ?>> primitivesValueOf = new HashMap<>();

    static {
        primitivesValueOf.put(Short.class, Short::valueOf);
        primitivesValueOf.put(Integer.class, Integer::valueOf);
        primitivesValueOf.put(Long.class, Long::valueOf);
        primitivesValueOf.put(Boolean.class, Boolean::valueOf);
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
        String inputName = getValidParameter(parameter);
        Object inputValue = inputs.get(inputName);

        if (inputValue == null) {
            return null;
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

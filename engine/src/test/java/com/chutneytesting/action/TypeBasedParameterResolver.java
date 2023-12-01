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

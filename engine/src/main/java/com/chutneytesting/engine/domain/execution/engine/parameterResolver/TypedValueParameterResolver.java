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

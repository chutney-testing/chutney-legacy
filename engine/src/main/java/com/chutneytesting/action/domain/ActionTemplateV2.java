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

package com.chutneytesting.action.domain;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.domain.parameter.ParameterResolver;
import com.chutneytesting.action.spi.Action;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ActionTemplateV2 implements ActionTemplate {

    private final String identifier;
    private final Class<? extends Action> implementationClass;
    private final Constructor<? extends Action> constructor;
    private final List<Parameter> parameters;

    public ActionTemplateV2(String identifier, Class<? extends Action> implementationClass, Constructor<? extends Action> constructor, List<Parameter> parameters) {
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
    public Class<? extends Action> implementationClass() {
        return implementationClass;
    }

    @Override
    public Set<Parameter> parameters() {
        return new LinkedHashSet<>(parameters);
    }

    @Override
    public Action create(List<ParameterResolver> parameterResolvers) {
        Object[] parameterValues = parameters.stream()
            .map(p -> resolveParameter(parameterResolvers, p))
            .toArray(Object[]::new);
        try {
            return constructor.newInstance(parameterValues);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ActionInstantiationFailureException(identifier, e);
        }
    }
}

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
import java.util.List;
import java.util.Set;

/**
 * Template for creating {@link Action} instances.<br>
 * This object exposes all parameters (names and types) needed by a {@link Action}
 */
public interface ActionTemplate {

    /**
     * @return an identifier to link action description in a scenario and its implementation
     */
    String identifier();

    /**
     * @return the class parsed into the current {@link ActionTemplate}. May not be a {@link Action} if adaptation is made to comply to the current SPI.
     */
    Class<?> implementationClass();

    /**
     * @return {@link Parameter}s needed to create an instance of {@link Action}
     */
    Set<Parameter> parameters();

    Action create(List<ParameterResolver> parameterResolvers) throws UnresolvableActionParameterException, ActionInstantiationFailureException;

    default <T> T resolveParameter(List<ParameterResolver> parameterResolvers, Parameter parameter) {
        return (T) parameterResolvers
            .stream()
            .filter(pr -> pr.canResolve(parameter))
            .findFirst()
            .orElseThrow(() -> new UnresolvableActionParameterException(identifier(), parameter))
            .resolve(parameter);
    }
}

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
import java.util.List;

/**
 * Thrown when a {@link ActionTemplate} fails to create a action
 * because a {@link Parameter} does not have a matching {@link ParameterResolver}.
 *
 * @see ActionTemplate#create(List)
 */
public class UnresolvableActionParameterException extends RuntimeException {

    private final String actionIdentifier;
    private final Parameter unresolvableParameter;

    public UnresolvableActionParameterException(String actionIdentifier, Parameter unresolvableParameter) {
        super("Unable to resolve " + unresolvableParameter + " of Action[" + actionIdentifier + "]");

        this.actionIdentifier = actionIdentifier;
        this.unresolvableParameter = unresolvableParameter;
    }

    public String actionIdentifier() {
        return actionIdentifier;
    }

    public Parameter unresolvableParameter() {
        return unresolvableParameter;
    }
}

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

/**
 * Error produced by a {@link ActionTemplateParser} if the parsing fails.
 */
public class ParsingError {
    private final Class<?> actionClass;
    private final String errorMessage;

    public ParsingError(Class<?> actionClass, String errorMessage) {
        this.actionClass = actionClass;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the class that failed to be parsed
     */
    public Class<?> actionClass() {
        return actionClass;
    }

    public String errorMessage() {
        return errorMessage;
    }
}

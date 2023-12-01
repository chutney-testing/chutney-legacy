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

package com.chutneytesting.server.core.domain.execution;

@SuppressWarnings("serial")
public class ScenarioConversionException extends RuntimeException {

    public ScenarioConversionException(String scenarioId, Exception e) {
        super("Unable to convert scenario [" + scenarioId + "]: " + e.getMessage(), e);
    }

    public ScenarioConversionException(String scenarioId, String message) {
        super("Unable to convert scenario [" + scenarioId + "]: " + message);
    }

    public ScenarioConversionException(Exception e) {
        super("Unable to convert scenario: " + e.getMessage(), e);
    }
}

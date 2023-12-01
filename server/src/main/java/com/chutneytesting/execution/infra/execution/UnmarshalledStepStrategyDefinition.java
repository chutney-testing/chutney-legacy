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

package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Map;

@JsonDeserialize(builder = UnmarshalledStepStrategyDefinition.UnmarshalledStepStrategyDefinitionBuilder.class)
class UnmarshalledStepStrategyDefinition {

    // TODO - because null is mapped to default strategy by the engine, but it shouldn't be
    public static final UnmarshalledStepStrategyDefinition NONE = null; // builder().build(); would be better

    public final String type;
    public final Map<String, Object> parameters;

    private UnmarshalledStepStrategyDefinition(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public static UnmarshalledStepStrategyDefinitionBuilder builder() {
        return new UnmarshalledStepStrategyDefinitionBuilder();
    }

    @JsonPOJOBuilder
    public static class UnmarshalledStepStrategyDefinitionBuilder {
        private String type;
        private Map<String, Object> parameters;

        private UnmarshalledStepStrategyDefinitionBuilder() {}

        public UnmarshalledStepStrategyDefinition build() {
            return new UnmarshalledStepStrategyDefinition(
              ofNullable(type).orElse("default"),
              ofNullable(parameters).orElse(emptyMap())
            );
        }

        public UnmarshalledStepStrategyDefinitionBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public UnmarshalledStepStrategyDefinitionBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
    }
}

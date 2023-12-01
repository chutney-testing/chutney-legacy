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

package com.chutneytesting.scenario.domain.raw;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RawTestCase implements TestCase {

    public final TestCaseMetadataImpl metadata;
    public final String scenario; // Blob
    private final Map<String, String> executionParameters;

    public RawTestCase(TestCaseMetadataImpl metadata, String scenario, Map<String, String> executionParameters) {
        this.metadata = metadata;
        this.scenario = scenario;
        this.executionParameters = executionParameters;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public Map<String, String> executionParameters() {
        return executionParameters;
    }

    @Override
    public TestCase usingExecutionParameters(Map<String, String> parameters) {
        return builder()
            .withMetadata(metadata)
            .withScenario(scenario)
            .withExecutionParameters(parameters)
            .build();
    }

    @Override
    public String toString() {
        return "RawTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + scenario +
            ", executionParameters=" + executionParameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawTestCase that = (RawTestCase) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(scenario, that.scenario) &&
            Objects.equals(executionParameters, that.executionParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, scenario, executionParameters);
    }

    public static RawTestCaseBuilder builder() {
        return new RawTestCaseBuilder();
    }

    public static class RawTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private String scenario;
        private Map<String, String> executionParameters;

        private RawTestCaseBuilder() {}

        public RawTestCase build() {
            return new RawTestCase(
                Optional.ofNullable(metadata).orElseGet(() -> TestCaseMetadataImpl.builder().build()),
                Optional.ofNullable(scenario).orElse(""),
                Optional.ofNullable(executionParameters).orElse(emptyMap())
            );
        }

        public RawTestCaseBuilder withMetadata(TestCaseMetadataImpl metadata) {
            this.metadata = metadata;
            return this;
        }

        public RawTestCaseBuilder withScenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        public RawTestCaseBuilder withExecutionParameters(Map<String, String> parameters) {
            this.executionParameters = unmodifiableMap(parameters);
            return this;
        }
    }
}

package com.chutneytesting.design.domain.scenario.raw;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
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

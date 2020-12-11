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
    private Map<String, String> parameters;

    public RawTestCase(TestCaseMetadataImpl metadata, String scenario, Map<String, String> parameters) {
        this.metadata = metadata;
        this.scenario = scenario;
        this.parameters = parameters;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public Map<String, String> parameters() {
        return parameters;
    }

    @Override
    public TestCase withParameters(Map<String, String> parameters) {
        return builder()
            .withMetadata(metadata)
            .withScenario(scenario)
            .withParameters(parameters)
            .build();
    }

    @Override
    public String toString() {
        return "RawTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + scenario +
            ", parameters=" + parameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawTestCase that = (RawTestCase) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(scenario, that.scenario) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, scenario, parameters);
    }

    public static RawTestCaseBuilder builder() {
        return new RawTestCaseBuilder();
    }

    public static class RawTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private String scenario;
        private Map<String, String> parameters;

        private RawTestCaseBuilder() {}

        public RawTestCase build() {
            return new RawTestCase(
                Optional.ofNullable(metadata).orElseGet(() -> TestCaseMetadataImpl.builder().build()),
                Optional.ofNullable(scenario).orElse(""),
                Optional.ofNullable(parameters).orElse(emptyMap())
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

        public RawTestCaseBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }
    }
}

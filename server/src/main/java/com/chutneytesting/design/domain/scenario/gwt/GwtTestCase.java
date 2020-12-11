package com.chutneytesting.design.domain.scenario.gwt;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.Map;
import java.util.Objects;

public class GwtTestCase implements TestCase {

    public final TestCaseMetadataImpl metadata;
    public final GwtScenario scenario;
    public final Map<String, String> parameters;

    private GwtTestCase(TestCaseMetadataImpl metadata, GwtScenario scenario, Map<String, String> parameters) {
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
        return "GwtTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + scenario +
            ", parameters=" + parameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GwtTestCase that = (GwtTestCase) o;
        return metadata.equals(that.metadata) &&
            scenario.equals(that.scenario) &&
            parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, scenario, parameters);
    }

    public static GwtTestCaseBuilder builder() {
        return new GwtTestCaseBuilder();
    }

    public static class GwtTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private GwtScenario scenario;
        private Map<String, String> parameters;

        private GwtTestCaseBuilder() {}

        public GwtTestCase build() {
            return new GwtTestCase(
                metadata,
                scenario,
                ofNullable(parameters).orElse(emptyMap())
            );
        }

        public GwtTestCaseBuilder withMetadata(TestCaseMetadataImpl metadata) {
            this.metadata = metadata;
            return this;
        }

        public GwtTestCaseBuilder withScenario(GwtScenario scenario) {
            this.scenario = scenario;
            return this;
        }

        public GwtTestCaseBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }
    }
}

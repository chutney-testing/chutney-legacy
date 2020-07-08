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
    public final Map<String, String> dataSet;

    private GwtTestCase(TestCaseMetadataImpl metadata, GwtScenario scenario, Map<String, String> dataSet) {
        this.metadata = metadata;
        this.scenario = scenario;
        this.dataSet = dataSet;
    }

    @Override
    public TestCaseMetadata metadata() {
        return metadata;
    }

    @Override
    public Map<String, String> computedParameters() {
        return dataSet;
    }

    @Override
    public TestCase withDataSet(Map<String, String> dataSet) {
        return builder()
            .withMetadata(metadata)
            .withScenario(scenario)
            .withDataSet(dataSet)
            .build();
    }

    @Override
    public String toString() {
        return "GwtTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + scenario +
            ", dataSet=" + dataSet +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GwtTestCase that = (GwtTestCase) o;
        return metadata.equals(that.metadata) &&
            scenario.equals(that.scenario) &&
            dataSet.equals(that.dataSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, scenario, dataSet);
    }

    public static GwtTestCaseBuilder builder() {
        return new GwtTestCaseBuilder();
    }

    public static class GwtTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private GwtScenario scenario;
        private Map<String, String> dataSet;

        private GwtTestCaseBuilder() {}

        public GwtTestCase build() {
            return new GwtTestCase(
                metadata,
                scenario,
                ofNullable(dataSet).orElse(emptyMap())
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

        public GwtTestCaseBuilder withDataSet(Map<String, String> dataSet) {
            this.dataSet = unmodifiableMap(dataSet);
            return this;
        }
    }
}

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
    public final String content; // Blob
    private Map<String, String> dataSet;

    public RawTestCase(TestCaseMetadataImpl metadata, String content, Map<String, String> dataSet) {
        this.metadata = metadata;
        this.content = content;
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
            .withScenario(content)
            .withDataSet(dataSet)
            .build();
    }

    @Override
    public String toString() {
        return "RawTestCase{" +
            "metadata=" + metadata +
            ", scenario=" + content +
            ", dataSet=" + dataSet +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawTestCase that = (RawTestCase) o;
        return Objects.equals(metadata, that.metadata) &&
            Objects.equals(content, that.content) &&
            Objects.equals(dataSet, that.dataSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, content, dataSet);
    }

    public static RawTestCaseBuilder builder() {
        return new RawTestCaseBuilder();
    }

    public static class RawTestCaseBuilder {

        private TestCaseMetadataImpl metadata;
        private String scenario;
        private Map<String, String> dataSet;

        private RawTestCaseBuilder() {}

        public RawTestCase build() {
            return new RawTestCase(
                Optional.ofNullable(metadata).orElseGet(() -> TestCaseMetadataImpl.builder().build()),
                Optional.ofNullable(scenario).orElse(""),
                Optional.ofNullable(dataSet).orElse(emptyMap())
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

        public RawTestCaseBuilder withDataSet(Map<String, String> dataSet) {
            this.dataSet = unmodifiableMap(dataSet);
            return this;
        }
    }
}

package com.chutneytesting.design.infra.storage.scenario.jdbc;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = TestCaseData.TestCaseDataBuilder.class)
public class TestCaseData {

    public final String version;

    public final String id;
    public final String title;
    public final String description;
    public final List<String> tags;
    public final Instant creationDate;

    public final Map<String, String> dataSet;
    public final String rawScenario;

    private TestCaseData(String version, String testCaseId, String title, String description, Instant creationDate, List<String> tags, Map<String, String> dataSet, String rawScenario) {
        this.version = version;
        this.id = testCaseId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.dataSet = dataSet;
        this.rawScenario = rawScenario;
    }

    @Override
    public String toString() {
        return "TestCaseData{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", tags=" + tags +
            ", creationDate=" + creationDate +
            ", dataSet=" + dataSet +
            ", rawScenario='" + rawScenario + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseData that = (TestCaseData) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(rawScenario, that.rawScenario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, rawScenario);
    }

    public static TestCaseDataBuilder builder() {
        return new TestCaseDataBuilder();
    }

    @JsonPOJOBuilder
    public static class TestCaseDataBuilder {

        private String version;
        private String testCaseId;
        private String title;
        private String description;
        private Instant creationDate;
        private List<String> tags;
        private Map<String, String> dataSet;
        private String rawScenario;

        public TestCaseData build() {
            return new TestCaseData(
                version,
                ofNullable(testCaseId).orElseThrow(IllegalArgumentException::new),
                ofNullable(title).orElse(""),
                ofNullable(description).orElse(""),
                ofNullable(creationDate).orElse(Instant.now()),
                unmodifiableList(ofNullable(tags).orElse(emptyList())),
                unmodifiableMap(ofNullable(dataSet).orElse(emptyMap())),
                ofNullable(rawScenario).orElse("")
            );
        }

        public TestCaseDataBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public TestCaseDataBuilder withId(String testCaseId) {
            this.testCaseId = testCaseId;
            return this;
        }

        public TestCaseDataBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TestCaseDataBuilder withCreationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public TestCaseDataBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TestCaseDataBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public TestCaseDataBuilder withDataSet(Map<String, String> dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        public TestCaseDataBuilder withRawScenario(String rawScenario) {
            this.rawScenario = rawScenario;
            return this;
        }
    }
}

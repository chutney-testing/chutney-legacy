package com.chutneytesting.design.infra.storage.scenario.jdbc;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

import com.chutneytesting.security.domain.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = TestCaseData.TestCaseDataBuilder.class)
public class TestCaseData {

    public final String contentVersion;

    public final String id;
    public final String title;
    public final String description;
    public final List<String> tags;
    public final Instant creationDate;
    public final Instant updateDate;
    public final String author;
    public final Integer version;

    public final Map<String, String> executionParameters;
    public final String rawScenario;

    private TestCaseData(String contentVersion, String testCaseId, String title, String description, Instant creationDate, List<String> tags, Map<String, String> executionParameters, String rawScenario, Instant updateDate, String author, Integer version) {
        this.contentVersion = contentVersion;
        this.id = testCaseId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.executionParameters = executionParameters;
        this.rawScenario = rawScenario;
        this.updateDate = updateDate;
        this.author = author;
        this.version = version;
    }

    @Override
    public String toString() {
        return "TestCaseData{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", tags=" + tags +
            ", creationDate=" + creationDate +
            ", executionParameters=" + executionParameters +
            ", rawScenario='" + rawScenario + '\'' +
            ", contentVersion='" + contentVersion + '\'' +
            ", updateDate='" + updateDate + '\'' +
            ", author='" + author + '\'' +
            ", version='" + version + '\'' +
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

        private String contentVersion;
        private String testCaseId;
        private String title;
        private String description;
        private Instant creationDate;
        private List<String> tags;
        private Map<String, String> executionParameters;
        private String rawScenario;
        private Instant updateDate;
        private String author;
        private Integer version;

        public TestCaseData build() {
            return new TestCaseData(
                contentVersion,
                ofNullable(testCaseId).orElseThrow(IllegalArgumentException::new),
                ofNullable(title).orElse(""),
                ofNullable(description).orElse(""),
                ofNullable(creationDate).orElse(Instant.now()),
                unmodifiableList(ofNullable(tags).orElse(emptyList())),
                unmodifiableMap(ofNullable(executionParameters).orElse(emptyMap())),
                ofNullable(rawScenario).orElse(""),
                ofNullable(updateDate).orElse(creationDate),
                ofNullable(author).orElseGet(User.ANONYMOUS_USER::getId),
                ofNullable(version).orElse(1)
            );
        }

        public TestCaseDataBuilder withContentVersion(String contentVersion) {
            this.contentVersion = contentVersion;
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

        public TestCaseDataBuilder withExecutionParameters(Map<String, String> executionParameters) {
            this.executionParameters = executionParameters;
            return this;
        }

        public TestCaseDataBuilder withRawScenario(String rawScenario) {
            this.rawScenario = rawScenario;
            return this;
        }

        public TestCaseDataBuilder withUpdateDate(Instant updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public TestCaseDataBuilder withAuthor(String author) {
            this.author = author;
            return this;
        }

        public TestCaseDataBuilder withVersion(Integer version) {
            this.version = version;
            return this;
        }

        public static TestCaseDataBuilder from(TestCaseData testCaseData) {
            return builder()
                .withId(testCaseData.id)
                .withContentVersion(testCaseData.contentVersion)
                .withTitle(testCaseData.title)
                .withDescription(testCaseData.description)
                .withCreationDate(testCaseData.creationDate)
                .withRawScenario(testCaseData.rawScenario)
                .withTags(testCaseData.tags)
                .withExecutionParameters(testCaseData.executionParameters)
                .withAuthor(testCaseData.author)
                .withUpdateDate(testCaseData.updateDate)
                .withVersion(testCaseData.version);
        }
    }
}

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

package com.chutneytesting.scenario.infra.raw;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.security.User;
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
    public final String defaultDataset;

    public final Map<String, String> executionParameters;
    public final String rawScenario;

    private TestCaseData(String contentVersion, String testCaseId, String title, String description, Instant creationDate, List<String> tags, Map<String, String> executionParameters, String rawScenario, Instant updateDate, String author, Integer version, String defaultDataset) {
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
        this.defaultDataset = defaultDataset;
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
        private String defaultDataset;

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
                ofNullable(author).orElseGet(() -> User.ANONYMOUS.id),
                ofNullable(version).orElse(1),
                ofNullable(defaultDataset).orElse(""));
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

        public TestCaseDataBuilder withDefaultDataset(String dataset) {
            this.defaultDataset = dataset;
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
                .withVersion(testCaseData.version)
                .withDefaultDataset(testCaseData.defaultDataset);
        }
    }
}

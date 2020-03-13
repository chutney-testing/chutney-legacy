package com.chutneytesting.design.domain.scenario;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TestCaseMetadataImpl implements TestCaseMetadata {

    public final String id;
    public final String title;
    public final String description;
    public final List<String> tags;
    public final Instant creationDate;

    public final String repositorySource;

    private TestCaseMetadataImpl(String id, String title, String description, List<String> tags, Instant creationDate, String repositorySource) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.repositorySource = repositorySource;
    }


    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Instant creationDate() {
        return creationDate;
    }

    @Override
    public List<String> tags() {
        return tags;
    }

    @Override
    public String repositorySource() {
        return repositorySource;
    }

    @Override
    public String toString() {
        return "GwtTestCaseMetadata{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", tags=" + tags +
            ", creationDate=" + creationDate +
            ", repositorySource=" + repositorySource +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseMetadataImpl that = (TestCaseMetadataImpl) o;
        return id.equals(that.id) &&
            title.equals(that.title) &&
            description.equals(that.description) &&
            tags.equals(that.tags) &&
            creationDate.equals(that.creationDate) &&
            repositorySource.equals(that.repositorySource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, tags, creationDate, repositorySource);
    }

    public static TestCaseMetadataBuilder builder() {
        return new TestCaseMetadataBuilder();
    }

    public static class TestCaseMetadataBuilder {
        private String id;
        private String title;
        private String description;
        private List<String> tags;
        private Instant creationDate;
        private String repositorySource;

        private TestCaseMetadataBuilder() {}

        public TestCaseMetadataImpl build() {
            return new TestCaseMetadataImpl(
                ofNullable(id).orElse("-42"),
                ofNullable(title).orElse(""),
                ofNullable(description).orElse(""),
                (ofNullable(tags).orElse(emptyList())).stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()),
                ofNullable(creationDate).orElse(Instant.now()),
                ofNullable(repositorySource).orElse(TestCaseRepository.DEFAULT_REPOSITORY_SOURCE)
            );
        }

        public TestCaseMetadataBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public TestCaseMetadataBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TestCaseMetadataBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TestCaseMetadataBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public TestCaseMetadataBuilder withCreationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public TestCaseMetadataBuilder withRepositorySource(String repositorySource) {
            this.repositorySource = repositorySource;
            return this;
        }

        public static TestCaseMetadataBuilder from(TestCaseMetadata testCaseMetadata) {
            return new TestCaseMetadataBuilder()
                .withId(testCaseMetadata.id())
                .withTitle(testCaseMetadata.title())
                .withDescription(testCaseMetadata.description())
                .withCreationDate(testCaseMetadata.creationDate())
                .withTags(testCaseMetadata.tags())
                .withRepositorySource(testCaseMetadata.repositorySource());
        }

    }

}

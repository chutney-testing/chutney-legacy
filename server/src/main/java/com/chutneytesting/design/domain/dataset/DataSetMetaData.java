package com.chutneytesting.design.domain.dataset;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataSetMetaData {

    public final String name;
    public final String description;
    public final Instant creationDate;
    public final List<String> tags;

    private DataSetMetaData(String name, String description, Instant creationDate, List<String> tags) {
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSetMetaData that = (DataSetMetaData) o;
        return name.equals(that.name) &&
            description.equals(that.description) &&
            creationDate.equals(that.creationDate) &&
            tags.equals(that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, creationDate, tags);
    }

    @Override
    public String toString() {
        return "DataSetMetaData{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", creationDate=" + creationDate +
            ", tags=" + tags +
            '}';
    }

    public static DataSetMetaDataBuilder builder() {
        return new DataSetMetaDataBuilder();
    }

    public static class DataSetMetaDataBuilder {
        private String name;
        private String description;
        private Instant creationDate;
        private List<String> tags;

        private DataSetMetaDataBuilder() {
        }

        public DataSetMetaData build() {
            if (Objects.isNull(name) || name.isEmpty()) {
                throw new IllegalArgumentException("DataSet name is mandatory");
            }

            return new DataSetMetaData(
                name,
                ofNullable(description).orElse(""),
                ofNullable(creationDate).orElseGet(Instant::now),
                (ofNullable(tags).orElse(emptyList())).stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList())
            );
        }

        public DataSetMetaDataBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DataSetMetaDataBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public DataSetMetaDataBuilder withCreationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public DataSetMetaDataBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public DataSetMetaDataBuilder fromDataSetMetaData(DataSetMetaData dataSetMetaData) {
            return new DataSetMetaDataBuilder()
                .withName(dataSetMetaData.name)
                .withDescription(dataSetMetaData.description)
                .withCreationDate(dataSetMetaData.creationDate)
                .withTags(dataSetMetaData.tags);
        }
    }
}

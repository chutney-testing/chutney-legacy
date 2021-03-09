package com.chutneytesting.design.domain.dataset;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class DataSet {

    public final String id;
    public final String name;
    public final String description;
    public final Instant creationDate;
    public final List<String> tags;
    public final Map<String, String> uniqueValues;
    public final List<Map<String, String>> multipleValues;

    private DataSet(String id, String name, String description, Instant creationDate, List<String> tags, Map<String, String> uniqueValues, List<Map<String, String>> multipleValues) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.tags = tags;
        this.uniqueValues = uniqueValues;
        this.multipleValues = multipleValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSet dataSet = (DataSet) o;

        return Objects.equals(id, dataSet.id) &&
            Objects.equals(name, dataSet.name) &&
            Objects.equals(description, dataSet.description) &&
            Objects.equals(creationDate, dataSet.creationDate) &&
            Objects.equals(tags, dataSet.tags) &&
            Objects.equals(uniqueValues, dataSet.uniqueValues) &&
            Objects.equals(multipleValues, dataSet.multipleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, creationDate, tags, uniqueValues, multipleValues);
    }

    @Override
    public String toString() {
        return "DataSet{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", creationDate=" + creationDate +
            ", tags=" + tags +
            ", uniqueValues=" + uniqueValues +
            ", multipleValues=" + multipleValues +
            '}';
    }

    public static DataSetBuilder builder() {
        return new DataSetBuilder();
    }

    public static class DataSetBuilder {
        private static String DEFAULT_ID = "-1";

        private String id;
        private String name;
        private String description;
        private Instant creationDate;
        private List<String> tags;
        private Map<String, String> uniqueValues;
        private List<Map<String, String>> multipleValues;

        private DataSetBuilder() {
        }

        public DataSet build() {
            if (!Objects.isNull(id) && id.isEmpty()) {
                throw new IllegalArgumentException("DataSet id cannot be empty");
            }

            return new DataSet(
                ofNullable(id).orElse(DEFAULT_ID),
                ofNullable(name).orElse(""),
                ofNullable(description).orElse(""),
                ofNullable(creationDate).orElseGet(() -> Instant.now().truncatedTo(MILLIS)),
                (ofNullable(tags).orElse(emptyList())).stream().map(String::toUpperCase).map(String::trim).collect(toList()),
                cleanUniqueValues(ofNullable(uniqueValues).orElse(emptyMap())),
                cleanMultipleValues(ofNullable(multipleValues).orElse(emptyList()))
            );
        }

        public DataSetBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataSetBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public DataSetBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public DataSetBuilder withCreationDate(Instant creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public DataSetBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public DataSetBuilder withUniqueValues(Map<String, String> uniqueValues) {
            this.uniqueValues = uniqueValues;
            return this;
        }

        public DataSetBuilder withMultipleValues(List<Map<String, String>> multipleValues) {
            this.multipleValues = multipleValues;
            return this;
        }

        public DataSetBuilder fromDataSet(DataSet dataset) {
            return new DataSetBuilder()
                .withId(dataset.id)
                .withName(dataset.name)
                .withDescription(dataset.description)
                .withCreationDate(dataset.creationDate)
                .withTags(dataset.tags)
                .withUniqueValues(dataset.uniqueValues)
                .withMultipleValues(dataset.multipleValues);
        }

        private Map<String, String> cleanUniqueValues(Map<String, String> uniqueValues) {
            // Get rid of empty keys
            return uniqueValues.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        private List<Map<String, String>> cleanMultipleValues(List<Map<String, String>> multipleValues) {
            // Get rid of empty keys and empty lines
            return multipleValues.stream()
                .map(this::cleanUniqueValues)
                .filter(this::hasValuesNotBlank)
                .collect(toList());
        }

        private boolean hasValuesNotBlank(Map<String, String> map) {
            return map.values().stream().anyMatch(StringUtils::isNotBlank);
        }
    }
}

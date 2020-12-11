package com.chutneytesting.design.domain.dataset;

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
    public final Map<String, String> constants;
    public final List<Map<String, String>> datatable;

    private DataSet(String id, String name, String description, Instant creationDate, List<String> tags, Map<String, String> constants, List<Map<String, String>> datatable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.tags = tags;
        this.constants = constants;
        this.datatable = datatable;
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
            Objects.equals(constants, dataSet.constants) &&
            Objects.equals(datatable, dataSet.datatable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, creationDate, tags, constants, datatable);
    }

    @Override
    public String toString() {
        return "DataSet{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", creationDate=" + creationDate +
            ", tags=" + tags +
            ", constants=" + constants +
            ", datatable=" + datatable +
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
        private Map<String, String> constants;
        private List<Map<String, String>> datatable;

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
                ofNullable(creationDate).orElseGet(Instant::now),
                (ofNullable(tags).orElse(emptyList())).stream().map(String::toUpperCase).map(String::trim).collect(toList()),
                cleanConstants(ofNullable(constants).orElse(emptyMap())),
                cleanDatatable(ofNullable(datatable).orElse(emptyList()))
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

        public DataSetBuilder withConstants(Map<String, String> constants) {
            this.constants = constants;
            return this;
        }

        public DataSetBuilder withDatatable(List<Map<String, String>> datatable) {
            this.datatable = datatable;
            return this;
        }

        public DataSetBuilder fromDataSet(DataSet dataset) {
            return new DataSetBuilder()
                .withId(dataset.id)
                .withName(dataset.name)
                .withDescription(dataset.description)
                .withCreationDate(dataset.creationDate)
                .withTags(dataset.tags)
                .withConstants(dataset.constants)
                .withDatatable(dataset.datatable);
        }

        private Map<String, String> cleanConstants(Map<String, String> constants) {
            // Remove empty keys
            return constants.entrySet().stream()
                .filter(e -> isNotBlank(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        private List<Map<String, String>> cleanDatatable(List<Map<String, String>> datatable) {
            // Remove empty keys and empty lines
            return datatable.stream()
                .map(this::cleanConstants)
                .filter(this::hasValuesNotBlank)
                .collect(toList());
        }

        private boolean hasValuesNotBlank(Map<String, String> map) {
            return map.values().stream().anyMatch(StringUtils::isNotBlank);
        }
    }
}

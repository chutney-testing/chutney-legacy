package com.chutneytesting.server.core.domain.dataset;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class DataSet {

    public static Comparator<DataSet> datasetComparator = Comparator.comparing(DataSet::getName, String.CASE_INSENSITIVE_ORDER);
    public static DataSet NO_DATASET = DataSet.builder().build();

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

    private String getName() {
        return name;
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
        private static final String DEFAULT_ID = "-1";

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
                throw new IllegalArgumentException("Dataset id cannot be empty");
            }

            return new DataSet(
                ofNullable(id).orElse(DEFAULT_ID), // TODO - after component deprecation : remove default id and use the name with '_'
                prettify(ofNullable(name).orElse("")),  // TODO - after component deprecation : throw if name is empty, but provide a Dataset.NoDataset null object
                ofNullable(description).orElse(""),
                ofNullable(creationDate).orElseGet(() -> Instant.now().truncatedTo(MILLIS)),
                (ofNullable(tags).orElse(emptyList())).stream().map(String::toUpperCase).map(String::strip).collect(toList()),
                cleanConstants(ofNullable(constants).orElse(emptyMap())),
                cleanDatatable(ofNullable(datatable).orElse(emptyList()))
            );
        }

        private String prettify(String name) {
            return name.strip().replaceAll(" +", " ");
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
                .collect(Collectors.toMap(e -> e.getKey().trim(), e -> e.getValue().trim()));
        }

        private List<Map<String, String>> cleanDatatable(List<Map<String, String>> datatable) {
            // Remove empty keys and empty lines
            return datatable.stream()
                .map(this::cleanConstants)
                .filter(this::linesWithAtLeastOneNonBlankValue)
                .map(m -> m.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().trim(), e -> e.getValue().trim())))
                .collect(toList());
        }

        private boolean linesWithAtLeastOneNonBlankValue(Map<String, String> map) {
            return map.values().stream().anyMatch(StringUtils::isNotBlank);
        }
    }
}

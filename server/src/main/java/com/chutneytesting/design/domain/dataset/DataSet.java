package com.chutneytesting.design.domain.jdd;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

public class DataSet {

    public final String id;
    public final DataSetMetaData metadata;
    public final Map<String, String> uniqueValues;
    public final List<Map<String, String>> multipleValues;

    private DataSet(String id, DataSetMetaData metadata, Map<String, String> uniqueValues, List<Map<String, String>> multipleValues) {
        this.id = id;
        this.metadata = metadata;
        this.uniqueValues = uniqueValues;
        this.multipleValues = multipleValues;
    }

    public boolean hasId() {
        return !id.equals(DataSetBuilder.DEFAULT_ID);
    }

    public String values(boolean pretty) {
        StringBuilder values = new StringBuilder();
        StringBuilder line = new StringBuilder();

        if (!uniqueValues.isEmpty()) {
            if (pretty) {
                int maxKeyLength = uniqueValues.keySet().stream().mapToInt(String::length).max().getAsInt();
                uniqueValues.forEach((k, v) -> {
                    line.append(k);
                    for (int i = k.length(); i < maxKeyLength; i++) {
                        line.append(" ");
                    }
                    line.append(" | ").append(v);
                    values.append(line.toString().trim()).append("\n");
                    line.setLength(0);
                });
                values.append("\n");
            } else {
                uniqueValues.forEach((k, v) -> {
                    line.append(k).append(" | ").append(v);
                    values.append(line.toString().trim()).append("\n");
                    line.setLength(0);
                });
                values.append("\n");
            }
        }

        if (!multipleValues.isEmpty()) {
            if (pretty) {
                List<Pair<String, Integer>> headers = new ArrayList<>();
                multipleValues.get(0).keySet().forEach(m ->
                    headers.add(Pair.of(m, multipleValues.stream().mapToInt(mm -> mm.get(m).length()).max().getAsInt()))
                );
                headers.forEach(h -> {
                    String header = h.getLeft();
                    line.append(" | ").append(header);
                    for (int i = header.length(); i < h.getRight(); i++) {
                        line.append(" ");
                    }
                });
                line.append(" |");
                values.append(line.toString().trim()).append("\n");
                line.setLength(0);
                multipleValues.forEach(m -> {
                    headers.forEach(h -> {
                        String value = m.get(h.getLeft());
                        line.append(" | ").append(value);
                        for (int j = value.length(); j < h.getRight(); j++) {
                            line.append(" ");
                        }
                    });
                    line.append(" |");
                    values.append(line.toString().trim()).append("\n");
                    line.setLength(0);
                });
            } else {
                List<String> headers = new ArrayList<>(multipleValues.get(0).keySet());
                headers.forEach(k -> line.append(" | ").append(k));
                line.append(" |");
                values.append(line.toString().trim()).append("\n");
                line.setLength(0);
                multipleValues.forEach(m -> {
                    headers.forEach(k -> line.append(" | ").append(m.get(k)));
                    line.append(" |");
                    values.append(line.toString().trim()).append("\n");
                    line.setLength(0);
                });
            }
        }

        return values.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSet dataSet = (DataSet) o;
        return id.equals(dataSet.id) &&
            metadata.equals(dataSet.metadata) &&
            uniqueValues.equals(dataSet.uniqueValues) &&
            multipleValues.equals(dataSet.multipleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, metadata, uniqueValues, multipleValues);
    }

    @Override
    public String toString() {
        return "DataSet{" +
            "id='" + id + '\'' +
            ", metadata=" + metadata +
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
        private DataSetMetaData metadata;
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
                ofNullable(metadata).orElseGet(() -> DataSetMetaData.builder().build()),
                ofNullable(uniqueValues).orElse(emptyMap()),
                ofNullable(multipleValues).orElse(emptyList())
            );
        }

        public DataSetBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataSetBuilder withMetaData(DataSetMetaData metaData) {
            this.metadata = metaData;
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
                .withMetaData(metadata)
                .withUniqueValues(dataset.uniqueValues)
                .withMultipleValues(dataset.multipleValues);
        }
    }
}

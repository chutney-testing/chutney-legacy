package com.chutneytesting.design.domain.jdd;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataSet {

    public final String name;
    public final String description;
    public final List<String> tags;
    public final Instant creationDate;
    public final String version;

    public final Map<String, String> uniqueValues;
    public final Set<Map<String, String>> multipleValues;

    private DataSet(String name, String description, List<String> tags, Instant creationDate, String version, Map<String, String> uniqueValues, Set<Map<String, String>> multipleValues) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.creationDate = creationDate;
        this.version = version;
        this.uniqueValues = uniqueValues;
        this.multipleValues = multipleValues;
    }

    public static DataSetBuilder builder() {
        return new DataSetBuilder();
    }

    private static class DataSetBuilder {

    }
}

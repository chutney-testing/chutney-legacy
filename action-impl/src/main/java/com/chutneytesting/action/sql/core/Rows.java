package com.chutneytesting.action.sql.core;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Rows {

    @JsonProperty
    private final List<Row> rows;

    public Rows(List<Row> rows) {
        this.rows = rows;
    }

    public Row get(int index) {
        if (rows.isEmpty()) {
            return new Row(Collections.emptyList());
        }
        return rows.get(index);
    }

    public List<Object> get(String header) {
        return rows.stream()
            .map(row -> row.get(header))
            .collect(toList());
    }

    public List<List<Object>> valuesOf(String... header) {
        return rows.stream().map(
            row ->  Arrays.stream(header).map(row::get).collect(toList())
        ).collect(toList());
    }

    public List<Map<String, Object>> asMap() {
        return rows.stream().map(Row::asMap).collect(toList());
    }

    public int count() {
        return rows.size();
    }

}

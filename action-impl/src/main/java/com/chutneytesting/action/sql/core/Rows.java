package com.chutneytesting.action.sql.core;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

public class Rows {
    private final List<Row> rows;

    public Rows(List<Row> rows) {
        this.rows = rows;
    }

    public List<Object> get(String column) {
        return rows.stream()
            .map(row -> row.get(column).value)
            .collect(toList());
    }

    public List<List<Object>> valuesOf(String... column) {
        return rows.stream().map(
            row ->  Arrays.stream(column).map(s -> row.get(s).value).collect(toList())
        ).collect(toList());
    }
}

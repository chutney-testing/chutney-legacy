package com.chutneytesting.action.sql.core;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Rows {

    private final List<Row> rows;

    public Rows(List<Row> rows) {
        this.rows = rows;
    }

    public Row get(int index) {
        return rows.get(index);
    }

    public List<Object> get(String column) {
        return rows.stream()
            .map(row -> row.get(column))
            .collect(toList());
    }

    public List<List<Object>> valuesOf(String... column) {
        return rows.stream().map(
            row ->  Arrays.stream(column).map(row::get).collect(toList())
        ).collect(toList());
    }

    public List<Map<String, Object>> asMap() {
        return rows.stream().map(Row::asMap).collect(toList());
    }

}

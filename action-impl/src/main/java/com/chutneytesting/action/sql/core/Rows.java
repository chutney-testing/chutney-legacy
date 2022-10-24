package com.chutneytesting.action.sql.core;

import java.util.List;
import java.util.stream.Collectors;

public class Rows {
    private final List<Row> rows;

    public Rows(List<Row> rows) {
        this.rows = rows;
    }

    public List<Object> get(String column) {
        return rows.stream()
            .map(row -> row.get(column).value)
            .collect(Collectors.toList());
    }
}

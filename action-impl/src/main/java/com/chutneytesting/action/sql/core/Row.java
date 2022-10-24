package com.chutneytesting.action.sql.core;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Row {
    final List<Cell> cells;

    public Row(List<Cell> values) {
        this.cells = values;
    }

    public Cell get(Column column) {
        return cells.stream()
            .filter(v -> v.column.equals(column))
            .findFirst()
            .orElse(Cell.NONE);
    }

    public Cell get(String header) {
        return cells.stream()
            .filter(v -> v.column.name.equals(header))
            .findFirst()
            .orElse(Cell.NONE);
    }

    public Cell get(int index) {
        return cells.stream()
            .filter(v -> v.column.index == index)
            .findFirst()
            .orElse(Cell.NONE);
    }

    public String print(Map<Column, Integer> maxLength) {
        StringBuilder sb = new StringBuilder();
        if (!cells.isEmpty()) {
            sb.append("|");
            cells.forEach(c ->
                sb.append(" ")
                .append(c.print(maxLength.get(c.column)))
                .append(" |")
            );
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return cells.equals(row.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells);
    }

    @Override
    public String toString() {
        return "Row{" +
            "cells=" + cells +
            '}';
    }

    public Map<String, Object> asMap() {
        return cells.stream()
            .collect(toMap(c -> c.column.name, c -> c.value, (c1, c2) -> c1, HashMap::new));
    }
}

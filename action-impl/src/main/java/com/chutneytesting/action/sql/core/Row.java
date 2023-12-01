/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.sql.core;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Row {
    @JsonProperty
    final List<Cell> cells;

    public Row(List<Cell> values) {
        this.cells = values;
    }

    public Object get(Column column) {
        return cells.stream()
            .filter(c -> c.column.equals(column))
            .findFirst()
            .orElse(Cell.NONE)
            .value;
    }

    public Object get(String header) {
        return cells.stream()
            .filter(c -> c.column.hasName(header))
            .findFirst()
            .orElse(Cell.NONE)
            .value;
    }

    public Object get(int index) {
        return cells.stream()
            .filter(c -> c.column.index == index)
            .findFirst()
            .orElse(Cell.NONE)
            .value;
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

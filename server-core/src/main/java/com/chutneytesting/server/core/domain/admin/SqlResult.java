package com.chutneytesting.server.core.domain.admin;

import java.util.List;
import java.util.Optional;

public class SqlResult {
    public final Optional<Integer> updatedRows;
    public final Optional<String> error;
    public final Optional<Table> table;

    public SqlResult(Optional<Integer> updatedRows, Optional<String> error, Optional<Table> table) {
        this.updatedRows = updatedRows;
        this.error = error;
        this.table = table;
    }

    public static SqlResult error(String error) {
        return new SqlResult(Optional.empty(), Optional.of(error), Optional.empty());
    }

    public static SqlResult updatedRows(int updateCount) {
        return new SqlResult(Optional.of(updateCount), Optional.empty(), Optional.empty());
    }

    public static SqlResult data(Table table) {
        return new SqlResult(Optional.empty(), Optional.empty(), Optional.of(table));
    }

    public static class Table {
        public final List<String> columnNames;
        public final List<Row> rows;

        public Table(List<String> columnNames, List<Row> rows) {
            this.columnNames = columnNames;
            this.rows = rows;
        }
    }

    public static class Row {
        public final List<String> values;

        public Row(List<String> values) {
            this.values = values;
        }
    }
}

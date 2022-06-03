package com.chutneytesting.task.sql.core;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Records {

    public final int affectedRows;
    public final List<String> headers;
    @Deprecated public final List<List<Object>> rows;

    public final List<Column> columns;
    public final List<Row> records;

    public Records(int affectedRows, List<Column> columns, List<Row> records) {
        this.affectedRows = affectedRows;
        this.columns = columns;
        this.records = records;

        this.headers = this.columns.stream().map(Column::name).collect(toList());
        this.rows = this.records.stream().map(r -> r.cells).map(l -> l.stream().map(c -> c.value).collect(toList())).collect(toList());
    }

    List<String> getHeaders() {
        return headers;
    }

    List<List<Object>> getRows() {
        return rows;
    }

    public int count() {
        return records.size();
    }

    public Object[][] toMatrix() {
        final Object[][] matrix = new Object[records.size()][columns.size()];
        for (int rowIndex = 0; rowIndex < records.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                matrix[rowIndex][columnIndex] = records.get(rowIndex).get(columnIndex).value;
            }
        }
        return matrix;
    }

    public String printable(int limit) {
        StringBuilder sb = new StringBuilder();
        Map<Column, Integer> maxColumnLength = maximumColumnLength(limit);
        sb.append(tableHeaders(maxColumnLength));
        sb.append(tableRows(limit, maxColumnLength));
        return sb.toString();
    }

    Map<Column, Integer> maximumColumnLength(int limit) {
        return columns.stream()
            .collect(Collectors.toMap(
                c -> c,
                c -> this.maximumLength(c, limit)
            ));
    }

    private int maximumLength(Column column, int limit) {
        return Math.max(
            column.name.length(),
            records.stream()
                .limit(limit)
                .map(r -> r.get(column).value.toString().length())
                .max(Integer::compare)
                .orElse(0)
        );
    }

    public String tableHeaders(Map<Column, Integer> maxColumnLength) {
        StringBuilder sb = new StringBuilder();
        if (columns.size() > 0) {
            sb.append("|");
            columns.forEach(column ->
                sb.append(" ")
                    .append(column.printHeader(maxColumnLength.get(column)))
                    .append(" |")
            );
            sb.append("\n");
            sb.append("-".repeat(sb.length() - 1));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String nWhitespaces(int i) {
        return " ".repeat(i);
    }

    public String tableRows(int limit, Map<Column, Integer> maximumColumnLength) {
        List<String> lines = records.stream()
            .limit(limit)
            .map(r -> r.print(maximumColumnLength))
            .collect(toList());
        StringBuilder sb = new StringBuilder();
        lines.forEach(sb::append);
        return sb.toString();
    }

    public String rowAsString(Map<String, Object> row, Map<String, Integer> maxColumnLength) {
        StringBuilder sb = new StringBuilder();
        if (!row.isEmpty()) {
            sb.append("|");
            row.forEach((k, v) ->
                sb.append(" ")
                    .append(v.toString())
                    .append(nWhitespaces(maxColumnLength.get(k) - v.toString().length()))
                    .append(" |")
            );
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Records{" +
            "affectedRows=" + affectedRows +
            ", headers=" + headers +
            ", records=" + records +
            '}';
    }

}

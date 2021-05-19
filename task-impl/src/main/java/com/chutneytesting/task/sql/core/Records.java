package com.chutneytesting.task.sql.core;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Records {

    public final int affectedRows;
    public final List<String> headers;
    public final List<List<Object>> rows;

    public Records(int affectedRows, List<String> headers, List<List<Object>> rows) {
        this.affectedRows = affectedRows;
        this.headers = headers;
        this.rows = rows;
    }

    public List<Map<String, Object>> toListOfMaps() {
        final List<Map<String, Object>> listOfMaps = new ArrayList<>(rows.size());
        for (List<Object> row : rows) {
            final Map<String, Object> aRow = new LinkedHashMap<>(headers.size());
            for (int j = 0; j < headers.size(); j++) {
                aRow.put(headers.get(j), row.get(j));
            }
            listOfMaps.add(aRow);
        }
        return listOfMaps;
    }

    public Object[][] toMatrix() {
        final Object[][] matrix = new Object[rows.size()][headers.size()];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                matrix[rowIndex][columnIndex] = rows.get(rowIndex).get(columnIndex);
            }
        }
        return matrix;
    }

    public String printable() {
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> maxColumnLength = maximumColumnLength();
        sb.append(tableHeaders(maxColumnLength));
        sb.append(tableRows(maxColumnLength));
        return sb.toString();
    }

    public Map<String, Integer> maximumColumnLength() {
        return headers.stream()
            .collect(Collectors.toMap(
                h -> h,
                this::maximumLength
            ));
    }

    private int maximumLength(String header) {
        List<Map<String, Object>> list = toListOfMaps();
        Integer integer = list.stream()
            .map(r -> r.get(header).toString().length())
            .max(Integer::compare)
            .orElse(0);

        return Math.max(header.length(), integer);
    }

    public String tableHeaders(Map<String, Integer> maxColumnLength) {
        StringBuilder sb = new StringBuilder();
        if (headers.size() > 0) {
            sb.append("|");
            headers.forEach(header ->
                sb.append(" ")
                         .append(header)
                         .append(nWhitespaces(maxColumnLength.get(header) - header.length()))
                         .append(" |")
            );
            sb.append("\n");
            sb.append("-".repeat(sb.length()-1));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String nWhitespaces(int i) {
        return " ".repeat(i);
    }

    public String tableRows(Map<String, Integer> maximumColumnLength) {
        List<Map<String, Object>> rows = this.toListOfMaps();
        List<String> lines = rows.stream().map(row -> rowAsString(row, maximumColumnLength)).collect(Collectors.toList());
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
        return MoreObjects.toStringHelper(this)
            .add("affectedRows", affectedRows)
            .add("headers", headers)
            .add("rows", rows)
            .toString();
    }
}

package com.chutneytesting.task.sql.core;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Records {

    public final int affectedRows;
    public final List<String> headers;
    public final List<List<Object>> rows;

    Records(int affectedRows, List<String> headers, List<List<Object>> rows) {
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("affectedRows",affectedRows)
            .add("headers",headers)
            .add("rows",rows)
            .toString();
    }
}

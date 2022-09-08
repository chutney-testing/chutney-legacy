package com.chutneytesting.action.sql.core;

import java.util.Objects;
import java.util.Optional;

public class Cell {
    static final Cell NONE = new Cell(Column.NONE, Optional.empty());

    public final Column column;
    public final Object value;

    public Cell(Column column, Object value) {
        this.column = column;
        this.value = value;
    }

    String print(int maxLength) {
        return value.toString() + " ".repeat(maxLength - value.toString().length());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return column.equals(cell.column) &&
            value.equals(cell.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, value);
    }

    @Override
    public String toString() {
        return "Cell{" +
            "column=" + column.name +
            ", value=" + value +
            '}';
    }
}

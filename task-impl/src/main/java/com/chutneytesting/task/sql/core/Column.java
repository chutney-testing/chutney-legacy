package com.chutneytesting.task.sql.core;

import java.util.Objects;

public class Column {
    static final Column NONE = new Column("", -1);

    public final String name;
    public final int index;

    public Column(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String name() {
        return this.name;
    }

    public String printHeader(int length) {
        return name + " ".repeat(length - name.length());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return index == column.index &&
            Objects.equals(name, column.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

    @Override
    public String toString() {
        return "Column{" +
            "name='" + name + '\'' +
            ", index=" + index +
            '}';
    }
}

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

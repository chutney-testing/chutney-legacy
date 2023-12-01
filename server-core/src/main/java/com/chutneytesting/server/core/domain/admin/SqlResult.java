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

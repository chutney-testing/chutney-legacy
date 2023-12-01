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

package com.chutneytesting.admin.infra;


import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.server.core.domain.admin.DatabaseAdminService;
import com.chutneytesting.server.core.domain.admin.SqlResult;
import com.chutneytesting.server.core.domain.tools.ImmutablePaginationRequestWrapperDto;
import com.chutneytesting.server.core.domain.tools.PaginatedDto;
import com.chutneytesting.server.core.domain.tools.PaginationRequestWrapperDto;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;

@DisplayName("DatabaseAdminServiceImplTest")
@ResourceLock("changelog")
@EnableH2MemTestInfra
public class DatabaseAdminServiceImplTest extends AbstractLocalDatabaseTest {

    private DatabaseAdminService sut;

    @BeforeEach
    public void beforeEach() {
        sut = new DatabaseAdminServiceImpl(localDataSource);
    }

    @Test
    public void should_set_error_when_execute_not_executable_query() {
        String statement = "not a statement";
        SqlResult sqlResult = sut.execute(statement);

        assertThat(sqlResult.error).hasValueSatisfying(new Condition<>(
            e -> e.contains("Unable to execute statement"),
            "error contains the right message"));
        assertThat(sqlResult.table).isEmpty();
        assertThat(sqlResult.updatedRows).isEmpty();
    }

    @Test
    public void should_set_update_count_when_execute_update_query() {
        SqlResult sqlResult = sut.execute("UPDATE SCENARIO SET TITLE = 'test' WHERE ID = 123456");

        assertThat(sqlResult.error).isEmpty();
        assertThat(sqlResult.table).isEmpty();
        assertThat(sqlResult.updatedRows).hasValue(0);
    }

    @Test
    public void should_limit_result_to_20_records_when_execute_select_query() {
        String selectFragment = "SELECT 3 AS FIRST, 'test' as SECONDD ";
        String sql = selectFragment
            + String.join("", Collections.nCopies(25, "UNION ALL " + selectFragment));

        SqlResult sqlResult = sut.execute(sql);

        assertThat(sqlResult.error).isEmpty();
        assertThat(sqlResult.updatedRows).isEmpty();
        assertThat(sqlResult.table).isPresent();
        assertThat(sqlResult.table.get().columnNames).containsExactlyInAnyOrder("FIRST", "SECONDD");
        assertThat(sqlResult.table.get().rows).hasSize(20);
        assertThat(sqlResult.table.get().rows.get(0).values).containsExactlyInAnyOrder("3", "test");
    }

    @Test
    public void should_set_error_when_paginate_not_executable_query() {
        // Given
        final Integer ITEM_PER_PAGE = 10;
        PaginationRequestWrapperDto<String> unvalidQuery = ImmutablePaginationRequestWrapperDto.<String>builder()
            .pageNumber(1)
            .elementPerPage(ITEM_PER_PAGE)
            .wrappedRequest(Optional.of("not a valid query"))
            .build();


        PaginatedDto<SqlResult> sqlResult = sut.paginate(unvalidQuery);

        assertThat(sqlResult.data().get(0).error).hasValueSatisfying(new Condition<>(
            e -> e.contains("Unable to execute statement"),
            "error contains the right message"));
        assertThat(sqlResult.data().get(0).table).isEmpty();
        assertThat(sqlResult.data().get(0).updatedRows).isEmpty();
    }

    @Test
    public void should_set_update_count_when_paginate_update_query() {
        SqlResult sqlResult = sut.execute("UPDATE SCENARIO SET TITLE = 'test' WHERE ID = 123456");

        assertThat(sqlResult.error).isEmpty();
        assertThat(sqlResult.table).isEmpty();
        assertThat(sqlResult.updatedRows).hasValue(0);
    }
}

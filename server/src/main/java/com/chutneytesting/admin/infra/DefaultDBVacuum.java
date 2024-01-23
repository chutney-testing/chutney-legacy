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

import static java.util.Collections.emptyMap;

import com.chutneytesting.admin.domain.DBVacuum;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultDBVacuum implements DBVacuum {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSourceProperties dsProperties;

    public DefaultDBVacuum(
        NamedParameterJdbcTemplate jdbcTemplate,
        DataSourceProperties dsProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.dsProperties = dsProperties;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void vacuum() {
        if (isSQLiteDriver()) {
            jdbcTemplate.update("VACUUM", emptyMap());
        } else {
            throw new UnsupportedOperationException("Database Vacuum is only supported for SQLite database");
        }
    }

    private boolean isSQLiteDriver() {
        String jdbcUrlDB = dsProperties.determineUrl().split(":")[1];
        return "sqlite".equals(jdbcUrlDB);
    }
}

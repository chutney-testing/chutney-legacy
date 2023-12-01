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

import com.chutneytesting.action.spi.injectable.Target;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;

public class DefaultSqlClientFactory implements SqlClientFactory {

    private final int DEFAULT_MAX_FETCH_SIZE = 1000;

    @Override
    public SqlClient create(Target target) {
        return this.doCreate(target);
    }

    private SqlClient doCreate(Target target) {
        Properties props = new Properties();
        props.put("jdbcUrl", target.property("jdbcUrl").orElse(target.uri().toString()));
        target.user().ifPresent(user -> props.put("username", user));
        target.userPassword().ifPresent(password -> props.put("password", password));

        props.putAll(target.prefixedProperties("dataSource."));
        final HikariConfig config = new HikariConfig(props);
        final HikariDataSource ds = new HikariDataSource(config);

        return new SqlClient(ds, target.numericProperty("maxFetchSize").map(Number::intValue).orElse(DEFAULT_MAX_FETCH_SIZE));
    }
}

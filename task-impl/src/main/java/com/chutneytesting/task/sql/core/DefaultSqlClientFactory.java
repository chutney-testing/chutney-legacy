package com.chutneytesting.task.sql.core;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Target;
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
        props.put("jdbcUrl", target.url());
        target.security().credential().ifPresent(credential -> props.put("username", credential.username()));
        target.security().credential().ifPresent(credential -> props.put("password", credential.password()));

        target.properties().forEach(props::put);
        final HikariConfig config = new HikariConfig(props);
        final HikariDataSource ds = new HikariDataSource(config);

        return new SqlClient(ds, ofNullable(target.properties().get("maxFetchSize")).map(Integer::getInteger).orElse(DEFAULT_MAX_FETCH_SIZE));
    }
}

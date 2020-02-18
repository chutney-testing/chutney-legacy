package com.chutneytesting.task.sql.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Properties;

public class DefaultSqlClientFactory implements SqlClientFactory {

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

        return new SqlClient(ds);
    }
}

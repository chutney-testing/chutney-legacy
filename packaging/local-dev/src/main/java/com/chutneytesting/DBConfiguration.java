package com.chutneytesting;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class DBConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConfiguration.class);

    private static final String DBSERVER_PORT_SPRING_VALUE = "${chutney.db-server.port}";
    private static final String DBSERVER_BASEDIR_SPRING_VALUE = "${chutney.db-server.base-dir:~/.chutney/data}";

    @Configuration
    @Profile("db-sqlite")
    static class SqliteConfiguration {

        @Value(DBSERVER_BASEDIR_SPRING_VALUE)
        private String baseDir;

        @PostConstruct
        public void initBaseDir() throws IOException {
            Files.createDirectories(Path.of(baseDir));
        }

        @Bean
        public DataSource dataSource(DataSourceProperties internalDataSourceProperties) {
            return internalDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        }
    }

    @Configuration
    @Profile("db-h2")
    static class H2Configuration {

        @Bean
        @Primary
        @ConfigurationProperties("spring.datasource")
        public DataSourceProperties internalDataSourceProperties() {
            return new DataSourceProperties() {
                @Override
                public String determineUsername() {
                    return this.getUsername();
                }

                @Override
                public String determinePassword() {
                    return this.getPassword();
                }
            };
        }

        @Bean(value = "dbServer", destroyMethod = "stop")
        Server dbServer(
            @Value(DBSERVER_PORT_SPRING_VALUE) int dbServerPort,
            @Value(DBSERVER_BASEDIR_SPRING_VALUE) String baseDir) throws SQLException {
            Server h2Server = Server.createTcpServer("-tcp", "-tcpPort", String.valueOf(dbServerPort), "-tcpAllowOthers", "-baseDir", baseDir, "-ifNotExists").start();
            LOGGER.debug("Started H2 server " + h2Server.getURL());
            return h2Server;
        }

        @Bean
        @DependsOn("dbServer")
        public DataSource dataSource(DataSourceProperties internalDataSourceProperties) {
            return internalDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        }
    }

    @Configuration
    @Profile("db-pg")
    static class PGConfiguration {

        @Bean
        public DataSource dataSource(DataSourceProperties internalDataSourceProperties) {
            return internalDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        }

    }
}

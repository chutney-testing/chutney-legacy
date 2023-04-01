package com.chutneytesting.tests;

import static com.chutneytesting.tests.AbstractLocalDatabaseTest.DB_CHANGELOG_DB_CHANGELOG_MASTER_XML;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Profile("test-infra")
class TestInfraConfiguration {

    @Configuration
    @Profile("test-infra-h2")
    static class H2Configuration {
        @Bean
        public DataSourceProperties dataSourceProperties() {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl("jdbc:h2:mem:test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
            return dataSourceProperties;
        }

        @Bean
        public Properties jpaProperties() {
            Properties jpaProperties = new Properties();
            jpaProperties.putAll(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
                "hibernate.show_sql", "true",
                "hibernate.use-new-id-generator-mappings", "false"
            ));
            return jpaProperties;
        }
    }

    @Configuration
    @Profile("test-infra-sqlite")
    static class SQLiteConfiguration {
        @Bean
        public DataSourceProperties dataSourceProperties() throws IOException {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            Path tmpDir = Files.createTempDirectory("test-infra-sqlite-");
            dataSourceProperties.setUrl("jdbc:sqlite:" + tmpDir.toAbsolutePath() + "/sqlitesample.db");
            return dataSourceProperties;
        }

        @Bean
        public Properties jpaProperties() {
            Properties jpaProperties = new Properties();
            jpaProperties.putAll(Map.of(
                "hibernate.dialect", "org.sqlite.hibernate.dialect.SQLiteDialect",
                "hibernate.show_sql", "true",
                "hibernate.use-new-id-generator-mappings", "false"
            ));
            return jpaProperties;
        }
    }

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(2);
        hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
        hikariConfig.setUsername(dataSourceProperties.getUsername());
        hikariConfig.setPassword(dataSourceProperties.getPassword());
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Properties jpaProperties) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.chutneytesting");
        factory.setDataSource(dataSource);
        factory.setJpaProperties(jpaProperties);
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    @Bean
    public Liquibase liquibase(DataSource ds) throws SQLException, LiquibaseException {
        Database liquidBaseDB = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));
        Liquibase liquibase = new Liquibase(DB_CHANGELOG_DB_CHANGELOG_MASTER_XML, new ClassLoaderResourceAccessor(), liquidBaseDB);
        liquibase.update();
        return liquibase;
    }
}

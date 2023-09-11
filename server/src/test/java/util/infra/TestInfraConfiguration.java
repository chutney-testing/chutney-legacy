package util.infra;

import static util.infra.AbstractLocalDatabaseTest.DB_CHANGELOG_DB_CHANGELOG_MASTER_XML;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.core.AbstractLogService;
import liquibase.logging.core.AbstractLogger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.ui.LoggerUIService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import util.SocketUtil;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpa
@Profile("test-infra")
class TestInfraConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestInfraConfiguration.class);

    @Configuration
    @Profile("test-infra-h2")
    static class H2Configuration {
        private static final Logger LOGGER = LoggerFactory.getLogger(H2Configuration.class);

        @Bean
        public DataSourceProperties inMemoryDataSourceProperties() {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl("jdbc:h2:mem:test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
            return dataSourceProperties;
        }

        @Bean
        @Profile("test-infra-h2-file")
        @Primary
        public DataSourceProperties fileDataSourceProperties(Server h2Server) {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl("jdbc:h2:tcp://localhost:" + h2Server.getPort() + "/./h2-chutney-171;SCHEMA=PUBLIC");
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

        @Bean(value = "dbServer", destroyMethod = "stop")
        Server dbServer() throws SQLException {
            int availablePort = SocketUtil.freePort();
            Path tempDirectory = copyH2ToTempDir();
            Server h2Server = Server.createTcpServer("-tcp", "-tcpPort", String.valueOf(availablePort), "-tcpAllowOthers", "-baseDir", tempDirectory.toString(), "-ifNotExists").start();
            LOGGER.debug("Started H2 server " + h2Server.getURL());
            return h2Server;
        }

        private Path copyH2ToTempDir() {
            File h2BaseBlankFile = new File(TestInfraConfiguration.class.getResource("/test-infra/h2-chutney-171.mv.db.blank.bak").getPath());
            Path tempDirectory;
            try {
                tempDirectory = Files.createTempDirectory("test-infra");
                Files.copy(h2BaseBlankFile.toPath(), tempDirectory.resolve("h2-chutney-171.mv.db"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return tempDirectory;
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
                "hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect",
                "hibernate.show_sql", "true",
                "hibernate.use-new-id-generator-mappings", "false"
            ));
            return jpaProperties;
        }

        @Bean
        public DataSource dataSource(DataSourceProperties dataSourceProperties) {
            LOGGER.info("test configuration datasource : {}", dataSourceProperties.getUrl());
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
            return new HikariDataSource(hikariConfig);
        }
    }

    @Configuration
    @Profile("test-infra-pgsql")
    static class PostgresConfiguration {

        @Bean(initMethod = "start", destroyMethod = "stop")
        public PostgreSQLContainer postgresDB() {
            return new PostgreSQLContainer(DockerImageName.parse("postgres:10.23-bullseye"));
        }

        @Bean
        public DataSourceProperties dataSourceProperties(PostgreSQLContainer postgresDB) {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl(postgresDB.getJdbcUrl());
            dataSourceProperties.setUsername(postgresDB.getUsername());
            dataSourceProperties.setPassword(postgresDB.getPassword());
            return dataSourceProperties;
        }

        @Bean
        public Properties jpaProperties() {
            Properties jpaProperties = new Properties();
            jpaProperties.putAll(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect",
                "hibernate.show_sql", "true"
            ));
            return jpaProperties;
        }
    }

    @Primary
    @Bean
    @Profile("!test-infra-sqlite")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        LOGGER.info("test configuration datasource : {}", dataSourceProperties.getUrl());
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(5);
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
    public Liquibase liquibase(
        DataSource ds,
        @Value("${chutney.test-infra.liquibase.run:true}") boolean liquibaseInit,
        @Value("${chutney.test-infra.liquibase.context:!test}") String initContext,
        @Value("${chutney.test-infra.liquibase.log.service:false}") boolean logService,
        @Value("${chutney.test-infra.liquibase.log.ui:true}") boolean logUi
    ) throws Exception {
        try (Connection conn = ds.getConnection()) {
            Database liquibaseDB = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            Liquibase liquibase = new Liquibase(DB_CHANGELOG_DB_CHANGELOG_MASTER_XML, new ClassLoaderResourceAccessor(), liquibaseDB);
            if (!logService) {
                Scope.enter(Map.of(Scope.Attr.logService.name(), new NoLiquibaseLogService()));
            }
            if (!logUi) {
                Scope.enter(Map.of(Scope.Attr.ui.name(), new LoggerUIService()));
            }
            if (liquibaseInit) {
                liquibase.update(initContext);
            }
            return liquibase;
        }
    }

    private static class NoLiquibaseLogService extends AbstractLogService {

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public liquibase.logging.Logger getLog(Class clazz) {
            return new AbstractLogger() {
                @Override
                public void log(Level level, String message, Throwable e) {

                }
            };
        }
    }
}

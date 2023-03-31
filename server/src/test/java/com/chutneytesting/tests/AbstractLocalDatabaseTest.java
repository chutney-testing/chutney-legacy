package com.chutneytesting.tests;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringJUnitConfig
@ActiveProfiles("test-infra")
public abstract class AbstractLocalDatabaseTest {
    private static final String DB_CHANGELOG_DB_CHANGELOG_MASTER_XML = "changelog/db.changelog-master.xml";
    private static final Random rand = new Random();

    @Autowired
    protected DataSource localDataSource;
    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private Liquibase liquibase;

    @AfterEach
    public void tearDown() {
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM SCENARIO_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_SCENARIOS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_PARAMETER");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN");
        jdbcTemplate.execute("DELETE FROM SCENARIO");
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @Profile("test-infra")
    static class AbstractLocalDatabaseConfiguration {
        @Bean
        public DataSource dataSource() {
            DataSourceProperties dsProps = new DataSourceProperties();
            dsProps.setUrl("jdbc:h2:mem:test_" + rand.nextInt(10000) + ";DB_CLOSE_DELAY=-1");
            return dsProps.initializeDataSourceBuilder().type(HikariDataSource.class).build();
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
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(false);

            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setJpaVendorAdapter(vendorAdapter);
            factory.setPackagesToScan("com.chutneytesting");
            factory.setDataSource(dataSource());
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

    protected void liquibaseUpdate() throws LiquibaseException {
        liquibase.update();
    }

    protected final void createCampaignWithScenarioExecution(Long campaignId, String scenarioId, Long scenarioExecutionId, Long campaignExecutionId) {
        Map<String, Object> scenarioIdParameter = new HashMap<>();
        scenarioIdParameter.put("campaignId", campaignId);
        scenarioIdParameter.put("scenarioId", scenarioId);
        scenarioIdParameter.put("scenarioExecutionId", scenarioExecutionId);
        scenarioIdParameter.put("campaignExecutionId", campaignExecutionId);

        namedParameterJdbcTemplate.update("insert into scenario_execution_history (id, scenario_id, report) values (:scenarioExecutionId, :scenarioId, '')", scenarioIdParameter);
        namedParameterJdbcTemplate.update("insert into campaign (id, title, description) values (:campaignId, 'test campaign', '')", scenarioIdParameter);
        namedParameterJdbcTemplate.update("insert into campaign_scenarios (campaign_id, scenario_id) values (:campaignId, :scenarioId)", scenarioIdParameter);
        namedParameterJdbcTemplate.update("insert into campaign_execution_history (campaign_id, id, scenario_id, scenario_execution_id) values (:campaignId, :campaignExecutionId, :scenarioId, :scenarioExecutionId)", scenarioIdParameter);
    }
}

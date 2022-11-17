package com.chutneytesting.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractLocalDatabaseTest {
    private static final String DB_CHANGELOG_DB_CHANGELOG_MASTER_XML = "changelog/db.changelog-master.xml";
    private static final Random rand = new Random();

    protected final DataSource localDataSource;
    protected final JdbcTemplate jdbcTemplate;
    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected final Database database;
    protected AbstractLocalDatabaseTest() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(2);
        hikariConfig.setJdbcUrl("jdbc:h2:mem:test_" + rand.nextInt(10000) + ";DB_CLOSE_DELAY=-1");
        localDataSource = new HikariDataSource(hikariConfig);
        jdbcTemplate = new JdbcTemplate(localDataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        try {
            this.database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(localDataSource.getConnection()));
            initializeLiquibase();
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("cannot initialize liquibase", e);
        }
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM SCENARIO_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_SCENARIOS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_PARAMETER");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN");
        jdbcTemplate.execute("DELETE FROM SCENARIO");
    }

    protected void initializeLiquibase() throws LiquibaseException {
        new Liquibase(DB_CHANGELOG_DB_CHANGELOG_MASTER_XML, new ClassLoaderResourceAccessor(), database).update("");
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

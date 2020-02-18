package com.chutneytesting.tests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.After;
import org.junit.rules.TemporaryFolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public abstract class AbstractLocalDatabaseTest {
    private static final String DB_CHANGELOG_DB_CHANGELOG_MASTER_XML = "changelog/db.changelog-master.xml";

    protected final DataSource localDataSource;
    protected final JdbcTemplate jdbcTemplate;
    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected AbstractLocalDatabaseTest() {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        String iceberTempFolderPath;
        try {
            temporaryFolder.create();
            iceberTempFolderPath = temporaryFolder.newFolder("temp_db", "ui").getAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot create temp folder for H2 database", e);
        }
        localDataSource = new SingleConnectionDataSource(
            new StringBuilder("jdbc:h2:mem:")
                .append(iceberTempFolderPath)
                .append("DB_CLOSE_DELAY=-1")
                .toString(), true);
        jdbcTemplate = new JdbcTemplate(localDataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        initializeLiquibase();
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM SCENARIO_EXECUTION_HISTORY");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_SCENARIOS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_PARAMETER");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN");
        jdbcTemplate.execute("DELETE FROM SCENARIO");
    }

    private void initializeLiquibase() {
        Database database;
        try {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(localDataSource.getConnection()));
            new Liquibase(DB_CHANGELOG_DB_CHANGELOG_MASTER_XML, new ClassLoaderResourceAccessor(), database).update("");
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("cannot initialize liquibase", e);
        }
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

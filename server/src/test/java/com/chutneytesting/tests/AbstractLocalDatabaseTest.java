package com.chutneytesting.tests;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ActiveProfiles("test-infra")
@ContextConfiguration(classes = TestInfraConfiguration.class)
public abstract class AbstractLocalDatabaseTest {
    protected static final String DB_CHANGELOG_DB_CHANGELOG_MASTER_XML = "changelog/db.changelog-master.xml";
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

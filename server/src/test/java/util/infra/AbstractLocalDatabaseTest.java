package util.infra;

import static java.time.Instant.now;

import com.chutneytesting.campaign.infra.jpa.Campaign;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecution;
import com.chutneytesting.scenario.infra.jpa.Scenario;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
    protected EntityManager entityManager;
    protected TransactionTemplate transactionTemplate = new TransactionTemplate();
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private Liquibase liquibase;

    @BeforeEach
    void setTransactionTemplate() {
        transactionTemplate.setTransactionManager(transactionManager);
    }

    protected void clearTables() {
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_EXECUTIONS");
        jdbcTemplate.execute("DELETE FROM SCENARIO_EXECUTIONS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_SCENARIOS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN_PARAMETERS");
        jdbcTemplate.execute("DELETE FROM CAMPAIGN");
        jdbcTemplate.execute("DELETE FROM SCENARIO");
    }

    protected void liquibaseUpdate() throws LiquibaseException {
        liquibase.update("!test");
    }

    protected Scenario givenScenario() {
        Scenario scenario = new Scenario(null, "", null, "", null, now(), null, true, null, now(), null);
        return transactionTemplate.execute(ts -> {
            entityManager.persist(scenario);
            return scenario;
        });
    }

    protected Campaign givenCampaign(Scenario... scenarios) {
        Campaign campaign = new Campaign("", new ArrayList<>());
        return transactionTemplate.execute(ts -> {
            campaign.scenarios().addAll(Arrays.asList(scenarios));
            entityManager.persist(campaign);
            return campaign;
        });
    }

    protected ScenarioExecution givenScenarioExecution(Long scenarioId, ServerReportStatus status) {
        ScenarioExecution execution = new ScenarioExecution(null, scenarioId, null, now().toEpochMilli(), 0L, status, null, null, "", "", "", null, null, null);
        return transactionTemplate.execute(ts -> {
            entityManager.persist(execution);
            return execution;
        });
    }

    protected List<String> scenariosIds(Scenario... scenarios) {
        return Arrays.stream(scenarios).map(Scenario::id).map(String::valueOf).toList();
    }
}

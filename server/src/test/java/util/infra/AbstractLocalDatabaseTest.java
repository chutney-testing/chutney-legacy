/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util.infra;

import static java.time.Instant.now;

import com.chutneytesting.campaign.infra.jpa.CampaignEntity;
import com.chutneytesting.campaign.infra.jpa.CampaignScenarioEntity;
import com.chutneytesting.execution.infra.storage.jpa.ScenarioExecutionEntity;
import com.chutneytesting.scenario.infra.jpa.ScenarioEntity;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import jakarta.persistence.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
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

    private Random rand = new Random();
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

    protected void liquibaseUpdate() throws LiquibaseException, SQLException {
        try (Connection conn = localDataSource.getConnection()) {
            liquibase.getDatabase().setConnection(new JdbcConnection(conn));
            liquibase.update("!test");
        }
    }

    protected ScenarioEntity givenScenario() {
        ScenarioEntity scenarioEntity = new ScenarioEntity(null, "", null, "{\"when\":{}}", null, now(), null, true, null, now(), null, null);
        return transactionTemplate.execute(ts -> {
            entityManager.persist(scenarioEntity);
            return scenarioEntity;
        });
    }

    protected String givenScenarioId() {
        return givenScenarioId(false);
    }

    protected String givenScenarioId(boolean componentId) {
        int clusterId = rand.nextInt(100);
        int objectId = rand.nextInt(100);
        return componentId ? clusterId + "-" + objectId : String.valueOf(objectId);
    }

    protected CampaignEntity givenCampaign(ScenarioEntity... scenarioEntities) {
        ArrayList<CampaignScenarioEntity> campaignScenarioEntities = new ArrayList<>();
        CampaignEntity campaign = new CampaignEntity("", campaignScenarioEntities);
        return transactionTemplate.execute(ts -> {
            for (int i = 0; i < scenarioEntities.length; i++) {
                ScenarioEntity scenarioEntity = scenarioEntities[i];
                campaignScenarioEntities.add(new CampaignScenarioEntity(campaign, scenarioEntity.getId().toString(), i));
            }
            campaign.campaignScenarios().addAll(campaignScenarioEntities);
            entityManager.persist(campaign);
            return campaign;
        });
    }

    protected ScenarioExecutionEntity givenScenarioExecution(Long scenarioId, ServerReportStatus status) {
        ScenarioExecutionEntity execution = new ScenarioExecutionEntity(null, scenarioId.toString(), null, now().toEpochMilli(), 0L, status, null, null, "", "", "", null, null, null);
        return transactionTemplate.execute(ts -> {
            entityManager.persist(execution);
            return execution;
        });
    }

    protected List<String> scenariosIds(ScenarioEntity... scenarioEntities) {
        return Arrays.stream(scenarioEntities).map(ScenarioEntity::getId).map(String::valueOf).toList();
    }
}

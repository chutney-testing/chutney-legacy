package com.chutneytesting.design.infra.storage.campaign;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.domain.campaign.Campaign;
import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.design.domain.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import com.chutneytesting.tests.AbstractLocalDatabaseTest;
import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CampaignExecutionRepositoryTest extends AbstractLocalDatabaseTest {

    private CampaignExecutionRepository sut;
    private Map<String, Map<Long, ExecutionHistory.ExecutionSummary>> scenarioExecutions;
    private Campaign currentCampaign;

    @BeforeEach
    public void setUp() {
        scenarioExecutions = new HashMap<>();
        sut = new CampaignExecutionRepository(namedParameterJdbcTemplate);
    }

    @Test
    public void should_persist_1_execution_when_saving_1_campaign_execution_report() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);
        insertScenarioExec(scenarioId, "4", "SUCCESS");

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", Lists.newArrayList(scenarioId), emptyMap(),LocalTime.now(), "env", false, false);
        saveOneCampaignExecutionReport(campaignId, 1L, scenarioId, scenarioName, 4, ServerReportStatus.SUCCESS);

        assertAllExecutionHistoryPersisted();
    }

    @Test
    public void should_persist_2_executions_when_saving_2_campaign_execution_report() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);
        insertScenarioExec(scenarioId, "4", "SUCCESS");
        insertScenarioExec(scenarioId, "5", "FAILURE");

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", Lists.newArrayList(scenarioId), emptyMap(), LocalTime.now(), "env", false, false);
        saveOneCampaignExecutionReport(campaignId, 1L, scenarioId, scenarioName, 4, ServerReportStatus.SUCCESS);
        saveOneCampaignExecutionReport(campaignId, 2L, scenarioId, scenarioName, 5, ServerReportStatus.FAILURE);

        assertAllExecutionHistoryPersisted();
    }

    @Test
    public void should_remove_all_campaign_executions_when_removing_campaign_execution_report() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", Lists.newArrayList(scenarioId), emptyMap(),  LocalTime.now(), "env", false, false);
        saveOneCampaignExecutionReport(campaignId, 1L, scenarioId, scenarioName, 4, ServerReportStatus.SUCCESS);

        sut.clearAllExecutionHistory(campaignId);

        List<CampaignExecutionReport> executionHistory = sut.findExecutionHistory(currentCampaign.id);
        assertThat(executionHistory).hasSize(0);

    }

    @Test
    public void should_get_2_last_campaign_report_created() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);
        insertScenarioExec(scenarioId, "4", "SUCCESS");
        insertScenarioExec(scenarioId, "5", "FAILURE");
        insertScenarioExec(scenarioId, "6", "FAILURE");
        insertScenarioExec(scenarioId, "7", "FAILURE");

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", Lists.newArrayList(scenarioId), emptyMap(),  LocalTime.now(), "env", false, false);
        saveOneCampaignExecutionReport(campaignId, 1L, scenarioId, scenarioName, 4, ServerReportStatus.SUCCESS);
        saveOneCampaignExecutionReport(campaignId, 2L, scenarioId, scenarioName, 5, ServerReportStatus.FAILURE);
        saveOneCampaignExecutionReport(campaignId, 3L, scenarioId, scenarioName, 6, ServerReportStatus.SUCCESS);
        saveOneCampaignExecutionReport(campaignId, 4L, scenarioId, scenarioName, 7, ServerReportStatus.FAILURE);

        List<CampaignExecutionReport> lastExecutions = sut.findLastExecutions(2L);

        assertThat(lastExecutions).hasSize(2);
        assertThat(lastExecutions.get(0).executionId).isEqualTo(3);
        assertThat(lastExecutions.get(1).executionId).isEqualTo(4);
    }

    private void saveOneCampaignExecutionReport(Long campaignId, Long campaignExecutionId, String scenarioId, String scenarioName, long scenarioExecutionId, ServerReportStatus status) {
        ExecutionHistory.ExecutionSummary execution = generateScenarioExecution(scenarioExecutionId, status);
        scenarioExecutions.putIfAbsent(scenarioId, new HashMap<>());
        scenarioExecutions.get(scenarioId).put(scenarioExecutionId, execution);
        ScenarioExecutionReportCampaign scenarioExecutionReport = new ScenarioExecutionReportCampaign(scenarioId, scenarioName, execution);

        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaignId, Collections.singletonList(scenarioExecutionReport), "title", false, "env");

        sut.saveCampaignReport(campaignId, campaignExecutionReport);
    }

    private void assertAllExecutionHistoryPersisted() {
        List<CampaignExecutionReport> campaignExecutionReports = sut.findExecutionHistory(currentCampaign.id);
        assertThat(campaignExecutionReports).hasSize(scenarioExecutions.values().iterator().next().size());
        campaignExecutionReports.forEach(savedCampaignExecutionReport -> {
            assertThat(savedCampaignExecutionReport.executionId).isGreaterThan(0L);
            savedCampaignExecutionReport.scenarioExecutionReports().forEach(scenarioExecutionReport -> {
                ExecutionHistory.ExecutionSummary exec = scenarioExecutions.get(scenarioExecutionReport.scenarioId).get(scenarioExecutionReport.execution.executionId());
                assertThat(scenarioExecutionReport.execution.executionId()).isEqualTo(exec.executionId());
                assertThat(scenarioExecutionReport.execution.status()).isEqualTo(exec.status());
                assertThat(scenarioExecutionReport.execution.environment()).isEqualTo(exec.environment());
            });
        });
    }

    private void insertCampaign(long campaignId) {
        jdbcTemplate.execute("INSERT INTO CAMPAIGN VALUES (" + campaignId + ", 'campagne 1', 'description...', '00:11', 'GLOBAL', false, false)");
    }

    private void insertScenario(String scenarioId, String scenarioName) {
        jdbcTemplate.execute("INSERT INTO SCENARIO (ID, TITLE, DESCRIPTION,CONTENT,VERSION) VALUES (" + scenarioId + ", '" + scenarioName + "', 'lol', 'truc', 'v2.1')");
    }

    private void insertScenarioExec(String scenarioId, String execid, String status) {
        jdbcTemplate.execute("INSERT INTO SCENARIO_EXECUTION_HISTORY"
            + "(ID, SCENARIO_ID, EXECUTION_TIME, DURATION, STATUS, INFORMATION, ERROR, REPORT, TEST_CASE_TITLE, ENVIRONMENT) VALUES "
            + "(" + execid + ", " + scenarioId + ",0,0,'" + status + "','','','','fake', 'default')");
    }

    private ExecutionHistory.ExecutionSummary generateScenarioExecution(long scenarioExecutionId, ServerReportStatus status) {
        return ImmutableExecutionHistory.ExecutionSummary.builder().executionId(scenarioExecutionId)
            .duration(3L)
            .time(LocalDateTime.now())
            .status(status)
            .testCaseTitle("fake")
            .environment("default")
            .build();
    }
}

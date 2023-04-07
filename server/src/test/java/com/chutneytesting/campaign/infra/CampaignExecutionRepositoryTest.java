package com.chutneytesting.campaign.infra;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.infra.TestCaseRepositoryAggregator;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.infra.AbstractLocalDatabaseTest;
import util.infra.EnableH2MemTestInfra;

@DisplayName("CampaignExecutionRepositoryTest")
@EnableH2MemTestInfra
public class CampaignExecutionRepositoryTest extends AbstractLocalDatabaseTest {

    private CampaignExecutionRepository sut;
    private Map<String, Map<Long, ExecutionHistory.ExecutionSummary>> scenarioExecutions;
    private Campaign currentCampaign;

    @BeforeEach
    public void setUp() {
        scenarioExecutions = new HashMap<>();
        TestCaseRepositoryAggregator testCaseRepositoryMock = mock(TestCaseRepositoryAggregator.class);
        CampaignExecutionReportMapper campaignExecutionReportMapper = new CampaignExecutionReportMapper(testCaseRepositoryMock);
        TestCase mockTestCase = mock(TestCase.class);
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.title()).thenReturn("scenario title");
        when(mockTestCase.metadata()).thenReturn(mockTestCaseMetadata);
        when(testCaseRepositoryMock.findById(any())).thenReturn(of(mockTestCase));
        sut = new CampaignExecutionRepository(namedParameterJdbcTemplate, campaignExecutionReportMapper);
    }

    @AfterEach
    void afterEach() {
        clearTables();
    }

    @Test
    public void should_persist_1_execution_when_saving_1_campaign_execution_report() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);
        insertScenarioExec(scenarioId, "4", "SUCCESS");

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", newArrayList(scenarioId), emptyMap(), "env", false, false, null, null);
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

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", newArrayList(scenarioId), emptyMap(), "env", false, false, "#2:87", null);
        saveOneCampaignExecutionReport(campaignId, 1L, scenarioId, scenarioName, 4, ServerReportStatus.SUCCESS);
        saveOneCampaignExecutionReport(campaignId, 2L, scenarioId, scenarioName, 5, ServerReportStatus.FAILURE);

        assertAllExecutionHistoryPersisted();
    }

    @Test
    public void campaign_execution_history_should_list_not_executed_scenarios() {
        // Given
        long campaignId = 1;
        String scenarioName = "test1";
        String firstScenarioId = "3";
        String secondScenarioId = "5";
        insertCampaign(campaignId);
        insertScenario(firstScenarioId, scenarioName);
        insertScenario(secondScenarioId, scenarioName);
        insertScenarioExec(firstScenarioId, "4", "STOPPED");

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", newArrayList(firstScenarioId, secondScenarioId), emptyMap(), "env", false, false, null, null);
        saveOneCampaignExecutionReport(campaignId, 1L, firstScenarioId, scenarioName, 4, ServerReportStatus.STOPPED);
        saveOneCampaignExecutionReport(campaignId, 1L, secondScenarioId, scenarioName, -1, ServerReportStatus.NOT_EXECUTED);

        // When
        List<CampaignExecutionReport> executionHistory = sut.findExecutionHistory(currentCampaign.id);

        // Then
        assertThat(executionHistory).hasSize(1);
        List<ScenarioExecutionReportCampaign> scenarioExecutionReportCampaigns = executionHistory.get(0).scenarioExecutionReports();
        assertThat(scenarioExecutionReportCampaigns).hasSize(2);
        assertThat(scenarioExecutionReportCampaigns.get(0).execution.executionId()).isEqualTo(4);
        assertThat(scenarioExecutionReportCampaigns.get(0).execution.status()).isEqualTo(ServerReportStatus.STOPPED);
        assertThat(scenarioExecutionReportCampaigns.get(1).execution.executionId()).isEqualTo(-1);
        assertThat(scenarioExecutionReportCampaigns.get(1).execution.status()).isEqualTo(ServerReportStatus.NOT_EXECUTED);
    }

    @Test
    public void should_remove_all_campaign_executions_when_removing_campaign_execution_report() {
        long campaignId = 1;
        String scenarioName = "test1";
        String scenarioId = "3";
        insertCampaign(campaignId);
        insertScenario(scenarioId, scenarioName);

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", newArrayList(scenarioId), emptyMap(), "env", false, false, null, null);
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

        currentCampaign = new Campaign(campaignId, "campaignName", "campaign description", newArrayList(scenarioId), emptyMap(), "env", false, false, null, null);
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

        CampaignExecutionReport campaignExecutionReport = new CampaignExecutionReport(campaignExecutionId, campaignId, singletonList(scenarioExecutionReport), "title", false, "env", "#2:87", 5, "user");

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
                assertThat(scenarioExecutionReport.execution.datasetId()).isEqualTo(exec.datasetId());
                assertThat(scenarioExecutionReport.execution.datasetVersion()).isEqualTo(exec.datasetVersion());
            });
        });
    }

    private void insertCampaign(long campaignId) {
        namedParameterJdbcTemplate.getJdbcTemplate()
            .execute("INSERT INTO CAMPAIGN VALUES (" + campaignId + ", 'campagne 1', 'description...', 'GLOBAL', false, false, '', '')");
    }

    private void insertScenario(String scenarioId, String scenarioName) {
        namedParameterJdbcTemplate.getJdbcTemplate()
            .execute("INSERT INTO SCENARIO "
                + "(ID, TITLE, DESCRIPTION, CONTENT, CONTENT_VERSION, CREATION_DATE, UPDATE_DATE, VERSION) VALUES "
                + " (" + scenarioId + ", '" + scenarioName + "', 'lol', 'truc', 'v2.1', 0, 0, 1)");
    }

    private void insertScenarioExec(String scenarioId, String execid, String status) {
        namedParameterJdbcTemplate.getJdbcTemplate()
            .execute("INSERT INTO SCENARIO_EXECUTION_HISTORY"
                + "(ID, SCENARIO_ID, EXECUTION_TIME, DURATION, STATUS, INFORMATION, ERROR, REPORT, TEST_CASE_TITLE, ENVIRONMENT, DATASET_ID, DATASET_VERSION) VALUES "
                + "(" + execid + ", " + scenarioId + ",0,0,'" + status + "','','','','fake', 'default', '#2:87', 5)");
    }

    private ExecutionHistory.ExecutionSummary generateScenarioExecution(long scenarioExecutionId, ServerReportStatus status) {
        return ImmutableExecutionHistory.ExecutionSummary.builder().executionId(scenarioExecutionId)
            .duration(3L)
            .time(LocalDateTime.now())
            .status(status)
            .testCaseTitle("fake")
            .environment("default")
            .datasetId("#2:87")
            .datasetVersion(5)
            .user("user")
            .build();
    }
}

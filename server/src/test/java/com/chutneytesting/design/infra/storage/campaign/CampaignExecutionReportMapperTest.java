package com.chutneytesting.design.infra.storage.campaign;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.campaign.CampaignExecutionReport;
import com.chutneytesting.execution.domain.history.ExecutionHistory;
import com.chutneytesting.execution.domain.history.ExecutionHistoryRepository;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CampaignExecutionReportMapperTest {
    private CampaignExecutionReportMapper sut;
    private ExecutionHistoryRepository scenarioExecutionHistoryRepository;

    @BeforeEach
    public void setUp() {
        scenarioExecutionHistoryRepository = Mockito.mock(ExecutionHistoryRepository.class);
        sut = new CampaignExecutionReportMapper();
    }

    private long campaignExecutionId = 3;
    private String scenarioId = "4";
    private long scenarioExecutionId = 5;
    private LocalDateTime started = LocalDateTime.now();
    private long duration = 6;
    private ServerReportStatus status = ServerReportStatus.SUCCESS;

    @Test
    public void extractDataWithOneScenarioExecution() throws SQLException {

        String scenarioName = "name";
        ResultSet resultSet = mockResultSet(campaignExecutionId, scenarioId, scenarioExecutionId, scenarioName);
        ExecutionHistory.Execution execution = getExecution(scenarioExecutionId, started, duration, status);
        when(scenarioExecutionHistoryRepository.getExecution(scenarioId, scenarioExecutionId)).thenReturn(execution);

        List<CampaignExecutionReport> campaignExecutionReports = sut.extractData(resultSet);

        Assertions.assertThat(campaignExecutionReports).allSatisfy(report -> {
            Assertions.assertThat(report.executionId).isEqualTo(campaignExecutionId);
            Assertions.assertThat(report.campaignName).isEqualTo("Title");
            Assertions.assertThat(report.scenarioExecutionReports()).allSatisfy(scenarioReport -> {
                Assertions.assertThat(scenarioReport.scenarioId).isEqualTo(scenarioId);
                Assertions.assertThat(scenarioReport.execution.executionId()).isEqualTo(scenarioExecutionId);
                Assertions.assertThat(scenarioReport.execution.status()).isEqualTo(status);
                Assertions.assertThat(scenarioReport.execution.duration()).isEqualTo(duration);
                Assertions.assertThat(scenarioReport.execution.time()).isEqualTo(started);
                Assertions.assertThat(scenarioReport.execution.environment()).isEqualTo("env");
            });
        });
    }

    private ResultSet mockResultSet(long campaignExecutionId, String scenarioId, long scenarioExecutionId, String scenarioName) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(rs.getLong("ID")).thenReturn(campaignExecutionId);
        when(rs.getString("SCENARIO_ID")).thenReturn(scenarioId);
        when(rs.getLong("SCENARIO_EXECUTION_ID")).thenReturn(scenarioExecutionId);
        when(rs.getString("TEST_CASE_TITLE")).thenReturn(scenarioName);
        when(rs.getString("CAMPAIGN_TITLE")).thenReturn("Title");
        when(rs.getBoolean("PARTIAL_EXECUTION")).thenReturn(false);

        when(rs.getLong("SCENARIO_EXECUTION_ID")).thenReturn(scenarioExecutionId);
        when(rs.getLong("EXECUTION_TIME")).thenReturn(started.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        when(rs.getLong("DURATION")).thenReturn(duration);
        when(rs.getString("STATUS")).thenReturn(status.name());
        when(rs.getString("INFORMATION")).thenReturn("");
        when(rs.getString("ERROR")).thenReturn("");
        when(rs.getString("TEST_CASE_TITLE")).thenReturn("fake");
        when(rs.getString("ENVIRONMENT")).thenReturn("env");
        return rs;
    }

    private ExecutionHistory.Execution getExecution(long executionId, LocalDateTime started, long duration, ServerReportStatus status) {
        return ImmutableExecutionHistory.Execution.builder()
            .duration(duration)
            .status(status)
            .time(started)
            .executionId(executionId)
            .report("")
            .testCaseTitle("fake")
            .environment("env")
            .build();
    }
}

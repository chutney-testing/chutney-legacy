package com.chutneytesting.campaign.infra;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.scenario.domain.TestCaseRepositoryAggregator;
import com.chutneytesting.server.core.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.scenario.TestCase;
import com.chutneytesting.server.core.scenario.TestCaseMetadata;
import com.chutneytesting.server.core.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.scenario.campaign.ScenarioExecutionReportCampaign;
import com.chutneytesting.tools.Try;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

public class CampaignExecutionReportMapperTest {

    private CampaignExecutionReportMapper sut;
    private TestCaseRepositoryAggregator testCaseRepositoryMock;

    @BeforeEach
    public void setUp() {
        testCaseRepositoryMock = mock(TestCaseRepositoryAggregator.class);
        sut = new CampaignExecutionReportMapper(testCaseRepositoryMock);
    }

    @Test
    public void extractDataWithOneScenarioExecution() throws SQLException {
        ScenarioExecutionReportCampaign scenarioExecutionReport = buildScenarioExecutionReportCampaign("scenarioId", "scenarioName");
        CampaignExecutionReport campaignExecutionReport = buildCampaignExecutionReport(
            1L,
            2L,
            singletonList(scenarioExecutionReport),
            "campaignName",
            false,
            "environment",
            "datasetId",
            666,
            "userId"
        );

        ResultSet resultSet = mockResultSet(singletonList(campaignExecutionReport));
        TestCase mockTestCase = mock(TestCase.class);
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.title()).thenReturn(scenarioExecutionReport.scenarioName);
        when(mockTestCase.metadata()).thenReturn(mockTestCaseMetadata);
        when(testCaseRepositoryMock.findById(any())).thenReturn(of(mockTestCase));

        List<CampaignExecutionReport> campaignExecutionReports = sut.extractData(resultSet);

        assertThat(campaignExecutionReports).allSatisfy(report -> {
            assertThat(report.executionId).isEqualTo(campaignExecutionReport.executionId);
            assertThat(report.campaignId).isEqualTo(campaignExecutionReport.campaignId);
            assertThat(report.campaignName).isEqualTo(campaignExecutionReport.campaignName);
            assertThat(report.executionEnvironment).isEqualTo(campaignExecutionReport.executionEnvironment);
            assertThat(report.startDate).isEqualTo(campaignExecutionReport.startDate);
            assertThat(report.partialExecution).isEqualTo(campaignExecutionReport.partialExecution);
            assertThat(report.dataSetId).isEqualTo(campaignExecutionReport.dataSetId);
            assertThat(report.dataSetVersion).isEqualTo(campaignExecutionReport.dataSetVersion);
            assertThat(report.userId).isEqualTo(campaignExecutionReport.userId);

            assertThat(report.scenarioExecutionReports()).allSatisfy(scenarioReport -> {
                assertThat(scenarioReport.scenarioId).isEqualTo(scenarioExecutionReport.scenarioId);
                assertThat(scenarioReport.scenarioName).isEqualTo(scenarioExecutionReport.scenarioName);
                assertThat(scenarioReport.execution.executionId()).isEqualTo(scenarioExecutionReport.execution.executionId());
                assertThat(scenarioReport.execution.status()).isEqualTo(scenarioExecutionReport.execution.status());
                assertThat(scenarioReport.execution.duration()).isEqualTo(scenarioExecutionReport.execution.duration());
                assertThat(scenarioReport.execution.time()).isEqualTo(scenarioExecutionReport.execution.time());
                assertThat(scenarioReport.execution.environment()).isEqualTo(scenarioExecutionReport.execution.environment());
                assertThat(scenarioReport.execution.datasetId()).hasValue(scenarioExecutionReport.execution.datasetId().toString());
                assertThat(scenarioReport.execution.datasetVersion()).hasValue(scenarioExecutionReport.execution.datasetVersion().get());
                assertThat(scenarioReport.execution.user()).isEqualTo(scenarioExecutionReport.execution.user());
            });
        });
    }

    @Test
    void should_not_explode_when_scenario_not_found() throws SQLException {
        ScenarioExecutionReportCampaign scenarioExecutionReport = buildScenarioExecutionReportCampaign("scenarioId", "scenarioName");
        ScenarioExecutionReportCampaign unknownScenarioExecutionReport = buildScenarioExecutionReportCampaign("unknownId", "unknownScenarioName");
        CampaignExecutionReport campaignExecutionReport = buildCampaignExecutionReport(
            1L,
            2L,
            asList(scenarioExecutionReport, unknownScenarioExecutionReport),
            "campaignName",
            false,
            "environment",
            "datasetId",
            666,
            "userId"
        );
        ResultSet resultSet = mockResultSet(singletonList(campaignExecutionReport));
        when(testCaseRepositoryMock.findById(unknownScenarioExecutionReport.scenarioId)).thenThrow(new ScenarioNotFoundException(unknownScenarioExecutionReport.scenarioId));
        TestCase mockTestCase = mock(TestCase.class);
        TestCaseMetadata mockTestCaseMetadata = mock(TestCaseMetadata.class);
        when(mockTestCaseMetadata.title()).thenReturn(scenarioExecutionReport.scenarioName);
        when(mockTestCase.metadata()).thenReturn(mockTestCaseMetadata);
        when(testCaseRepositoryMock.findById(scenarioExecutionReport.scenarioId)).thenReturn(of(mockTestCase));

        List<CampaignExecutionReport> campaignExecutionReports = sut.extractData(resultSet);

        assertThat(campaignExecutionReports).allSatisfy(report -> {
            assertThat(report.executionId).isEqualTo(campaignExecutionReport.executionId);
            assertThat(report.campaignId).isEqualTo(campaignExecutionReport.campaignId);
            assertThat(report.campaignName).isEqualTo(campaignExecutionReport.campaignName);
            assertThat(report.executionEnvironment).isEqualTo(campaignExecutionReport.executionEnvironment);
            assertThat(report.startDate).isEqualTo(campaignExecutionReport.executionEnvironment);
            assertThat(report.partialExecution).isEqualTo(campaignExecutionReport.partialExecution);
            assertThat(report.dataSetId).isEqualTo(campaignExecutionReport.dataSetId);
            assertThat(report.dataSetVersion).isEqualTo(campaignExecutionReport.dataSetVersion);
            assertThat(report.userId).isEqualTo(campaignExecutionReport.userId);

            assertThat(report.scenarioExecutionReports()).allSatisfy(scenarioReport -> {
                assertThat(scenarioReport.scenarioId).isEqualTo(scenarioExecutionReport.scenarioId);
                assertThat(scenarioReport.scenarioName).isEqualTo(scenarioExecutionReport.scenarioName);
                assertThat(scenarioReport.execution.executionId()).isEqualTo(scenarioExecutionReport.execution.executionId());
                assertThat(scenarioReport.execution.status()).isEqualTo(scenarioExecutionReport.execution.status());
                assertThat(scenarioReport.execution.duration()).isEqualTo(scenarioExecutionReport.execution.duration());
                assertThat(scenarioReport.execution.time()).isEqualTo(scenarioExecutionReport.execution.time());
                assertThat(scenarioReport.execution.environment()).isEqualTo(scenarioExecutionReport.execution.environment());
                assertThat(scenarioReport.execution.datasetId()).hasValue(scenarioExecutionReport.execution.datasetId().toString());
                assertThat(scenarioReport.execution.datasetVersion()).hasValue(scenarioExecutionReport.execution.datasetVersion().get());
                assertThat(scenarioReport.execution.user()).isEqualTo(scenarioExecutionReport.execution.user());
            });
        });
    }

    private ResultSet mockResultSet(List<CampaignExecutionReport> campaignExecutionReports) {
        ResultSet rs = mock(ResultSet.class);
        Try.exec(() -> {
            OngoingStubbing<Boolean> whenNext = when(rs.next());
            OngoingStubbing<Long> whenId = when(rs.getLong("ID"));
            OngoingStubbing<String> whenScenarioId = when(rs.getString("SCENARIO_ID"));
            OngoingStubbing<Long> whenScenarioExecutionId = when(rs.getLong("SCENARIO_EXECUTION_ID"));
            OngoingStubbing<String> whenTestCaseTitle = when(rs.getString("TEST_CASE_TITLE"));
            OngoingStubbing<String> whenCampaignTitle = when(rs.getString("CAMPAIGN_TITLE"));
            OngoingStubbing<Boolean> whenPartialExecution = when(rs.getBoolean("PARTIAL_EXECUTION"));
            OngoingStubbing<Long> whenExecutionTime = when(rs.getLong("EXECUTION_TIME"));
            OngoingStubbing<Long> whenDuration = when(rs.getLong("DURATION"));
            OngoingStubbing<String> whenStatus = when(rs.getString("STATUS"));
            OngoingStubbing<String> whenInformation = when(rs.getString("INFORMATION"));
            OngoingStubbing<String> whenError = when(rs.getString("ERROR"));
            OngoingStubbing<String> whenEnvironment = when(rs.getString("ENVIRONMENT"));
            OngoingStubbing<String> whenDatasetId = when(rs.getString("DATASET_ID"));
            OngoingStubbing<String> whenDatasetVersion = when(rs.getString("DATASET_VERSION"));
            OngoingStubbing<String> whenUserId = when(rs.getString("USER_ID"));

            campaignExecutionReports.forEach(cer -> cer.scenarioExecutionReports().forEach(ser -> {
                whenNext.thenReturn(true);
                whenId.thenReturn(cer.executionId);
                whenCampaignTitle.thenReturn(cer.campaignName);
                whenPartialExecution.thenReturn(cer.partialExecution);
                whenEnvironment.thenReturn(cer.executionEnvironment);

                whenScenarioId.thenReturn(ser.scenarioId);
                whenScenarioExecutionId.thenReturn(ser.execution.executionId());
                whenExecutionTime.thenReturn(ser.execution.time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                whenTestCaseTitle.thenReturn(ser.scenarioName);
                whenDuration.thenReturn(ser.execution.duration());
                whenStatus.thenReturn(ser.status().name());
                whenInformation.thenReturn(ser.execution.info().get());
                whenError.thenReturn(ser.execution.error().get());
                whenDatasetId.thenReturn(ser.execution.datasetId().toString());
                whenDatasetVersion.thenReturn(ser.execution.datasetVersion().get().toString());
                whenUserId.thenReturn(ser.execution.user());
            }));

            whenNext.thenReturn(false);

            return null;
        });
        return rs;
    }

    private CampaignExecutionReport buildCampaignExecutionReport(Long executionId, Long campaignId, List<ScenarioExecutionReportCampaign> scenarioExecutionReports, String campaignName, boolean partialExecution, String executionEnvironment, String datasetId, Integer datasetVersion, String userId) {
        return new CampaignExecutionReport(executionId, campaignId, scenarioExecutionReports, campaignName, partialExecution, executionEnvironment, datasetId, datasetVersion, userId);
    }

    private ScenarioExecutionReportCampaign buildScenarioExecutionReportCampaign(String scenarioId, String scenarioName) {
        ImmutableExecutionHistory.ExecutionSummary execution = ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(666L)
            .time(LocalDateTime.now())
            .duration(666)
            .status(ServerReportStatus.SUCCESS)
            .info("info")
            .error("error")
            .testCaseTitle("testcaseTitle")
            .environment("environment")
            .datasetId("datasetId")
            .datasetVersion(666)
            .user("userId")
            .build();

        return new ScenarioExecutionReportCampaign(scenarioId, scenarioName, execution);
    }
}

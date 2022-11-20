package com.chutneytesting.execution.infra.storage;

import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import com.chutneytesting.server.core.domain.scenario.campaign.CampaignExecutionReport;
import com.chutneytesting.server.core.domain.scenario.campaign.ScenarioExecutionReportCampaign;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;

class ExecutionSummaryRowMapper implements RowMapper<ExecutionSummary> {

    @Override
    public ExecutionSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ImmutableExecutionHistory.ExecutionSummary.builder()
            .executionId(rs.getLong("ID"))
            .time(Instant.ofEpochMilli(rs.getLong("EXECUTION_TIME")).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(rs.getLong("DURATION"))
            .status(ServerReportStatus.valueOf(rs.getString("STATUS")))
            .info(ofNullable(rs.getString("INFORMATION")))
            .error(ofNullable(rs.getString("ERROR")))
            .testCaseTitle(rs.getString("TEST_CASE_TITLE"))
            .environment((rs.getString("ENVIRONMENT")))
            .datasetId(ofNullable(rs.getString("DATASET_ID")))
            .datasetVersion(ofNullable(rs.getString("DATASET_VERSION")).map(Integer::valueOf))
            .user((rs.getString("USER_ID")))
            .campaignReport(mapCampaignExecutionReport(rs))
            .build();
    }

    private Optional<CampaignExecutionReport> mapCampaignExecutionReport(ResultSet rs){
        CampaignExecutionReport report = null;
        try {
            if (rs.getLong("CAMPAIGN_EXECUTION_ID") > 0) {
                report = new CampaignExecutionReport(
                    rs.getLong("CAMPAIGN_EXECUTION_ID"),
                    rs.getLong("CAMPAIGN_ID"),
                    new ArrayList<>(),
                    rs.getString("CAMPAIGN_TITLE"),
                    false,
                    null,
                    null,
                    null,
                    null);
            }

        } catch (SQLException e) {

        }
        return Optional.ofNullable(report);
    }
}

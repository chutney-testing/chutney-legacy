package com.chutneytesting.execution.infra.storage;

import static java.util.Optional.ofNullable;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.scenario.campaign.Campaign;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
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
            .campaign(getCampaignTitle(rs))
            .build();
    }

    private Optional<String> getCampaignTitle(ResultSet rs) {
        try {
            return Optional.ofNullable(rs.getString("CAMPAIGN_TITLE"));
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}

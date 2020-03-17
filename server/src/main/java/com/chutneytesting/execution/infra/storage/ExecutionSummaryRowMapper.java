package com.chutneytesting.execution.infra.storage;

import static java.util.Optional.ofNullable;

import com.chutneytesting.execution.domain.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.execution.domain.history.ImmutableExecutionHistory;
import com.chutneytesting.execution.domain.report.ServerReportStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
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
            .build();
    }
}

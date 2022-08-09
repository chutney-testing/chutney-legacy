package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;

public class ExecutionRowMapper implements RowMapper<Execution> {
    @Override
    public Execution mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(rs.getLong("ID"))
            .time(Instant.ofEpochMilli(rs.getLong("EXECUTION_TIME")).atZone(ZoneId.systemDefault()).toLocalDateTime())
            .duration(rs.getLong("DURATION"))
            .status(ServerReportStatus.valueOf(rs.getString("STATUS")))
            .info(Optional.ofNullable(rs.getString("INFORMATION")))
            .error(Optional.ofNullable(rs.getString("ERROR")))
            .report(rs.getString("REPORT"))
            .testCaseTitle(rs.getString("TEST_CASE_TITLE"))
            .environment(rs.getString("ENVIRONMENT"))
            .user(rs.getString("USER_ID"))
            .build();
    }
}

package com.chutneytesting.execution.infra.storage;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.DetachedExecution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.Execution;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.history.ExecutionHistoryRepository;
import com.chutneytesting.server.core.domain.execution.history.ImmutableExecutionHistory;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import com.google.common.collect.ImmutableMap;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class DatabaseExecutionHistoryRepository implements ExecutionHistoryRepository {

    private static final int LIMIT_BLOC_SIZE = 20;
    private final ExecutionRowMapper executionRowMapper = new ExecutionRowMapper();
    private final ExecutionSummaryRowMapper executionSummaryRowMapper = new ExecutionSummaryRowMapper();
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    DatabaseExecutionHistoryRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ExecutionSummary> getExecutions(String scenarioId) {
        return namedParameterJdbcTemplate.query(
            "SELECT SCENARIO_HIST.ID, SCENARIO_HIST.EXECUTION_TIME, SCENARIO_HIST.DURATION, SCENARIO_HIST.STATUS, SCENARIO_HIST.INFORMATION, SCENARIO_HIST.ERROR, " +
                "SCENARIO_HIST.TEST_CASE_TITLE, SCENARIO_HIST.ENVIRONMENT,SCENARIO_HIST.DATASET_ID, SCENARIO_HIST.DATASET_VERSION, SCENARIO_HIST.USER_ID, " +
                "CAMP.TITLE AS CAMPAIGN_TITLE, " +
                "CAMP.ID AS CAMPAIGN_ID, " +
                "CAMP_HIST.ID AS CAMPAIGN_EXECUTION_ID " +
                "FROM SCENARIO_EXECUTION_HISTORY SCENARIO_HIST " +
                "LEFT JOIN CAMPAIGN_EXECUTION_HISTORY CAMP_HIST ON CAMP_HIST.SCENARIO_EXECUTION_ID = SCENARIO_HIST.ID " +
                "LEFT JOIN CAMPAIGN CAMP ON CAMP.ID = CAMP_HIST .CAMPAIGN_ID " +
                "WHERE SCENARIO_HIST.SCENARIO_ID = :scenarioId ORDER BY SCENARIO_HIST.ID DESC LIMIT " + LIMIT_BLOC_SIZE,
            ImmutableMap.<String, Object>builder().put("scenarioId", scenarioId).build(),
            executionSummaryRowMapper);
    }

    @Override
    public ExecutionSummary getExecutionSummary(String scenarioId, Long executionId) {
        return namedParameterJdbcTemplate.queryForObject(
            "SELECT SCENARIO_HIST.ID, SCENARIO_HIST.EXECUTION_TIME, SCENARIO_HIST.DURATION, SCENARIO_HIST.STATUS, SCENARIO_HIST.INFORMATION, SCENARIO_HIST.ERROR, " +
                "SCENARIO_HIST.TEST_CASE_TITLE, SCENARIO_HIST.ENVIRONMENT,SCENARIO_HIST.DATASET_ID, SCENARIO_HIST.DATASET_VERSION, SCENARIO_HIST.USER_ID, " +
                "CAMP.TITLE AS CAMPAIGN_TITLE, " +
                "CAMP.ID AS CAMPAIGN_ID, " +
                "CAMP_HIST.ID AS CAMPAIGN_EXECUTION_ID " +
                "FROM SCENARIO_EXECUTION_HISTORY SCENARIO_HIST " +
                "LEFT JOIN CAMPAIGN_EXECUTION_HISTORY CAMP_HIST ON CAMP_HIST.SCENARIO_EXECUTION_ID = SCENARIO_HIST.ID " +
                "LEFT JOIN CAMPAIGN CAMP ON CAMP.ID = CAMP_HIST .CAMPAIGN_ID " +
                "WHERE SCENARIO_HIST.SCENARIO_ID = :scenarioId " +
                "AND SCENARIO_HIST.ID = :executionId",
            ImmutableMap.<String, Object>builder().put("scenarioId", scenarioId)
                .put("executionId", executionId).build(),
            executionSummaryRowMapper);
    }

    @Override
    public Execution store(String scenarioId, DetachedExecution detachedExecution) throws IllegalStateException {
        long nextId = namedParameterJdbcTemplate.queryForObject("SELECT nextval('SCENARIO_EXECUTION_HISTORY_SEQ')", Collections.emptyMap(), long.class);
        Execution execution = detachedExecution.attach(nextId);
        Map<String, Object> executionParameters = executionParameters(execution);
        executionParameters.put("scenarioId", scenarioId);
        executionParameters.put("id", nextId);
        namedParameterJdbcTemplate.update("INSERT INTO SCENARIO_EXECUTION_HISTORY"
                + "(ID, SCENARIO_ID, EXECUTION_TIME, DURATION, STATUS, INFORMATION, ERROR, REPORT, TEST_CASE_TITLE, ENVIRONMENT, DATASET_ID, DATASET_VERSION, USER_ID) VALUES "
                + "(:id, :scenarioId, :executionTime, :duration, :status, :information, :error, :report, :title, :environment, :datasetId, :datasetVersion, :user)",
            executionParameters);

        return ImmutableExecutionHistory.Execution.builder()
            .from(execution)
            .executionId(nextId)
            .build();
    }

    @Override
    public Execution getExecution(String scenarioId, Long reportId) throws ReportNotFoundException {
        try {
            return namedParameterJdbcTemplate.queryForObject(
                "SELECT ID, EXECUTION_TIME, DURATION, STATUS, INFORMATION, ERROR, REPORT, TEST_CASE_TITLE, ENVIRONMENT, DATASET_ID, DATASET_VERSION, USER_ID FROM SCENARIO_EXECUTION_HISTORY WHERE ID = :reportId AND SCENARIO_ID = :scenarioId",
                ImmutableMap.<String, Object>builder()
                    .put("reportId", reportId)
                    .put("scenarioId", scenarioId)
                    .build(),
                executionRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new ReportNotFoundException(scenarioId, reportId);
        }
    }

    @Override
    public void update(String scenarioId, Execution updatedExecution) throws ReportNotFoundException {
        int updatedEntries = update(updatedExecution);

        if (updatedEntries == 0) {
            throw new ReportNotFoundException(scenarioId, updatedExecution.executionId());
        }
    }

    private int update(Execution updatedExecution) throws ReportNotFoundException {
        Map<String, Object> executionParameters = executionParameters(updatedExecution);
        executionParameters.put("id", updatedExecution.executionId());

        return namedParameterJdbcTemplate.update(
            "UPDATE SCENARIO_EXECUTION_HISTORY SET "
                + "EXECUTION_TIME = :executionTime, DURATION = :duration, STATUS = :status, INFORMATION = :information, ERROR = :error, REPORT = :report "
                + "WHERE ID = :id",
            executionParameters);
    }

    @Override
    public int setAllRunningExecutionsToKO() {
        List<ExecutionSummary> runningExecutions = getExecutionsWithStatus(ServerReportStatus.RUNNING);
        updateExecutionsToKO(runningExecutions);

        List<ExecutionSummary> pausedExecutions = getExecutionsWithStatus(ServerReportStatus.PAUSED);
        updateExecutionsToKO(pausedExecutions);

        return runningExecutions.size() + pausedExecutions.size();
    }

    @Override
    public List<ExecutionSummary> getExecutionsWithStatus(ServerReportStatus status) {
        return namedParameterJdbcTemplate.query(
            "SELECT ID, EXECUTION_TIME, DURATION, STATUS, INFORMATION, ERROR, TEST_CASE_TITLE, ENVIRONMENT, DATASET_ID, DATASET_VERSION, USER_ID FROM SCENARIO_EXECUTION_HISTORY WHERE STATUS = :status",
            ImmutableMap.<String, Object>builder().put("status", status.name()).build(),
            executionSummaryRowMapper);
    }

    private void updateExecutionsToKO(List<ExecutionSummary> executions) {
        executions.stream()
            .map(this::buildKnockoutExecutionFrom)
            .forEach(this::update);
    }

    private ImmutableExecutionHistory.Execution buildKnockoutExecutionFrom(ExecutionSummary executionSummary) {
        return ImmutableExecutionHistory.Execution.builder()
            .executionId(executionSummary.executionId())
            .status(ServerReportStatus.FAILURE)
            .time(executionSummary.time())
            .duration(executionSummary.duration())
            .info(executionSummary.info())
            .error("Execution was interrupted !")
            .report("")
            .testCaseTitle(executionSummary.testCaseTitle())
            .environment(executionSummary.environment())
            .user(executionSummary.user())
            .build();
    }

    private Map<String, Object> executionParameters(Execution execution) {
        Map<String, Object> executionParameters = new HashMap<>();
        executionParameters.put("executionTime", execution.time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        executionParameters.put("duration", execution.duration());
        executionParameters.put("status", execution.status().name());
        executionParameters.put("information", execution.info().map(info -> StringUtils.substring(info, 0, 512)).orElse(null));
        executionParameters.put("error", execution.error().map(error -> StringUtils.substring(error, 0, 512)).orElse(null));
        executionParameters.put("report", execution.report());
        executionParameters.put("title", execution.testCaseTitle());
        executionParameters.put("environment", execution.environment());
        executionParameters.put("datasetId", execution.datasetId().orElse(null));
        executionParameters.put("datasetVersion", execution.datasetVersion().orElse(null));
        executionParameters.put("user", execution.user());
        return executionParameters;
    }
}

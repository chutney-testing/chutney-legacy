package com.chutneytesting.server.core.domain.execution.history;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory.ExecutionSummary;
import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository storing execution executionHistory by scenario.
 **/
public interface ExecutionHistoryRepository {

    /**
     * Add a report for a given scenario.
     *
     * @return execution ID
     * @throws IllegalStateException when storage for scenario cannot be created
     **/
    ExecutionHistory.Execution store(String scenarioId, ExecutionHistory.DetachedExecution executionProperties) throws IllegalStateException;

    /**
     * @param scenarioIds
     * @return the last report. Key of the map are scenarioIds
     */
    Map<String, ExecutionSummary> getLastExecutions(List<String> scenarioIds);

    /**
     * @return last reports of the indicated scenario.
     **/
    List<ExecutionSummary> getExecutions(String scenarioId);

    List<ExecutionSummary> getExecutions();

    ExecutionSummary getExecutionSummary(Long executionId);

    /**
     * @return the matching {@link ExecutionHistory.Execution}
     */
    ExecutionHistory.Execution getExecution(String scenarioId, Long reportId) throws ReportNotFoundException;

    /**
     * Override a previously stored {@link ExecutionHistory.Execution}.
     */
    void update(String scenarioId, ExecutionHistory.Execution updatedExecution);

    int setAllRunningExecutionsToKO();

    List<ExecutionSummary> getExecutionsWithStatus(ServerReportStatus status);

    ExecutionSummary deleteExecution(Long executionId);

    Set<ExecutionSummary> deleteExecutions(Set<Long> executionsIds);
}

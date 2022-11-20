package com.chutneytesting.server.core.domain.execution.history;

import com.chutneytesting.server.core.domain.execution.report.ReportNotFoundException;
import com.chutneytesting.server.core.domain.execution.report.ServerReportStatus;
import java.util.List;

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
     * @return last reports of the indicated scenario.
     **/
    List<ExecutionHistory.ExecutionSummary> getExecutions(String scenarioId);
    ExecutionHistory.ExecutionSummary getExecutionSummary(String scenarioId, Long executionId);

    /**
     * @return the matching {@link ExecutionHistory.Execution}
     */
    ExecutionHistory.Execution getExecution(String scenarioId, Long reportId) throws ReportNotFoundException;

    /**
     * Override a previously stored {@link ExecutionHistory.Execution}.
     */
    void update(String scenarioId, ExecutionHistory.Execution updatedExecution);

    int setAllRunningExecutionsToKO();

    List<ExecutionHistory.ExecutionSummary> getExecutionsWithStatus(ServerReportStatus status);
}

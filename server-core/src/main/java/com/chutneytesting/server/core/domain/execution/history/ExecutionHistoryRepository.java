/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    List<ExecutionHistory.ExecutionSummary> getExecutionReportMatchQuery(String query);

    /**
     * Override a previously stored {@link ExecutionHistory.Execution}.
     */
    void update(String scenarioId, ExecutionHistory.Execution updatedExecution);

    int setAllRunningExecutionsToKO();

    List<ExecutionSummary> getExecutionsWithStatus(ServerReportStatus status);

    void deleteExecutions(Set<Long> executionsIds);
}

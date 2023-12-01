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

package com.chutneytesting.server.core.domain.execution;

import com.chutneytesting.server.core.domain.execution.history.ExecutionHistory;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessors;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;


public class ScenarioExecutionEngine {

    private final ServerTestEngine executionEngine;
    private final TestCasePreProcessors testCasePreProcessors;
    private final ScenarioExecutionEngineAsync executionEngineAsync;


    public ScenarioExecutionEngine(ServerTestEngine executionEngine,
                                   TestCasePreProcessors testCasePreProcessors,
                                   ScenarioExecutionEngineAsync executionEngineAsync) {
        this.executionEngineAsync = executionEngineAsync;
        this.executionEngine = executionEngine;
        this.testCasePreProcessors = testCasePreProcessors;
    }

    /**
     * Retrieves a scenario from it's ID, executes it on  ExecutionEngine and store StepExecutionReport.
     *
     * @param executionRequest The request execution.
     * @return an execution Report.
     */
    public ScenarioExecutionReport execute(ExecutionRequest executionRequest) throws ScenarioNotFoundException, ScenarioNotParsableException {
        return executionEngineAsync.followExecution(executionRequest.testCase.id(), executionEngineAsync.execute(executionRequest)).blockingLast();
    }

    public ScenarioExecutionReport simpleSyncExecution(ExecutionRequest executionRequest) {
        ExecutionRequest processedExecutionRequest = new ExecutionRequest(testCasePreProcessors.apply(executionRequest), executionRequest.environment, executionRequest.userId, executionRequest.dataset);

        StepExecutionReportCore finalStepReport = executionEngine.execute(processedExecutionRequest);
        return new ScenarioExecutionReport(0L, processedExecutionRequest.testCase.metadata().title(), executionRequest.environment, executionRequest.userId, finalStepReport);
    }

    public ExecutionHistory.Execution saveNotExecutedScenarioExecution(ExecutionRequest executionRequest) {
        return executionEngineAsync.saveNotExecutedScenarioExecution(executionRequest);
    }

}

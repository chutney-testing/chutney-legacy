package com.chutneytesting.server.core.domain.execution;

import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessors;
import com.chutneytesting.server.core.domain.execution.report.ScenarioExecutionReport;
import com.chutneytesting.server.core.domain.execution.report.StepExecutionReportCore;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.server.core.domain.scenario.ScenarioNotParsableException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;


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
}

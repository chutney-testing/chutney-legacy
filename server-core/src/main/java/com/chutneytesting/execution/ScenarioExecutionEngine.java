package com.chutneytesting.execution;

import com.chutneytesting.execution.processor.TestCasePreProcessors;
import com.chutneytesting.execution.report.ScenarioExecutionReport;
import com.chutneytesting.execution.report.StepExecutionReportCore;
import com.chutneytesting.scenario.ScenarioNotFoundException;
import com.chutneytesting.scenario.ScenarioNotParsableException;
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
    public ScenarioExecutionReport execute(ExecutionRequest executionRequest, Optional<Pair<String, Integer>> executionDataset) throws ScenarioNotFoundException, ScenarioNotParsableException {
        return executionEngineAsync.followExecution(executionRequest.testCase.id(), executionEngineAsync.execute(executionRequest, executionDataset)).blockingLast();
    }

    public ScenarioExecutionReport simpleSyncExecution(ExecutionRequest executionRequest) {
        ExecutionRequest processedExecutionRequest = new ExecutionRequest(testCasePreProcessors.apply(executionRequest), executionRequest.environment, executionRequest.userId);

        StepExecutionReportCore finalStepReport = executionEngine.execute(processedExecutionRequest);
        return new ScenarioExecutionReport(0L, processedExecutionRequest.testCase.metadata().title(), executionRequest.environment, executionRequest.userId, finalStepReport);
    }
}

package com.chutneytesting.execution.domain.scenario;

import com.chutneytesting.design.api.scenario.v2_0.mapper.GwtScenarioMapper;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.scenario.ScenarioNotFoundException;
import com.chutneytesting.design.domain.scenario.ScenarioNotParsableException;
import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.gwt.GwtScenario;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.GwtScenarioMarshaller;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.report.ScenarioExecutionReport;
import com.chutneytesting.execution.domain.report.StepExecutionReportCore;
import java.util.Collections;
import java.util.Map;


public class ScenarioExecutionEngine {

    private final ServerTestEngine executionEngine;
    private final TestCasePreProcessors testCasePreProcessors;
    private ScenarioExecutionEngineAsync executionEngineAsync;
    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

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
     * @param testCase The TestCase to be execute.
     * @return an execution Report.
     */
    public ScenarioExecutionReport execute(TestCase testCase, String environment) throws ScenarioNotFoundException, ScenarioNotParsableException {
        return executionEngineAsync.followExecution(testCase.id(), executionEngineAsync.execute(new ExecutionRequest(testCase, environment))).blockingLast();
    }

    /**
     * @param content the scenario content to be executed
     * @param dataSet the scenario variables
     * @return an execution report
     */
    public ScenarioExecutionReport execute(String content, Map<String, String> dataSet, String environment) {
        GwtScenario gwtScenario = marshaller.deserialize("test title for idea", "test description for idea", content);
        TestCase testCase = GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withDescription("test description for idea")
                .withTitle("test title for idea")
                .build())
            .withScenario(gwtScenario)
            .withDataSet(dataSet)
            .build();
        return simpleSyncExecution(
            new ExecutionRequest(testCase, environment)
        );
    }

    public ScenarioExecutionReport execute(FunctionalStep functionalStep, String environment) throws ScenarioNotFoundException, ScenarioNotParsableException {
        TestCase testCase = new ComposableTestCase(
            "no_scenario_id",
            TestCaseMetadataImpl.builder()
                .withDescription(functionalStep.id + "-" + functionalStep.name)
                .withTitle(functionalStep.id + "-" + functionalStep.name)
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(Collections.singletonList(functionalStep))
                .build());

        return simpleSyncExecution(
            new ExecutionRequest(testCase, environment)
        );
    }

    private ScenarioExecutionReport simpleSyncExecution(ExecutionRequest executionRequest) {
        ExecutionRequest processedExecutionRequest = new ExecutionRequest(testCasePreProcessors.apply(executionRequest), executionRequest.environment);

        StepExecutionReportCore finalStepReport = executionEngine.execute(processedExecutionRequest);
        return new ScenarioExecutionReport(0L, processedExecutionRequest.testCase.metadata().title(), executionRequest.environment, finalStepReport);
    }
}

package com.chutneytesting.junit.engine;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import io.reactivex.Observable;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;

public class ChutneyEngineExecutionContext implements EngineExecutionContext {

    private final ExecutionConfiguration executionConfiguration;
    private ExecutionRequest executionRequest;

    protected ChutneyEngineExecutionContext(ExecutionConfiguration executionConfiguration, ExecutionRequest executionRequest) {
        this.executionConfiguration = executionConfiguration;
        this.executionRequest = executionRequest;
    }

    protected Observable<StepExecutionReport> executeScenario(StepDefinition stepDefinition) {
        Long executionId = executionConfiguration.executionEngine().execute(stepDefinition, ScenarioExecution.createScenarioExecution());
        return executionConfiguration.reporter().subscribeOnExecution(executionId);
    }


}

package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.engine.Dataset;

public interface ExecutionEngine {

    Long execute(StepDefinition stepDefinition, Dataset dataset, ScenarioExecution execution);

}

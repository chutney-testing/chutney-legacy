package com.chutneytesting.engine.domain.execution;

public interface ExecutionEngine {

    Long execute(StepDefinition stepDefinition, ScenarioExecution execution);

}

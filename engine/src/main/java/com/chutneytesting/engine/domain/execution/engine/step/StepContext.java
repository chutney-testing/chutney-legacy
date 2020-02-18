package com.chutneytesting.engine.domain.execution.engine.step;

import java.util.Map;

public interface StepContext {

    Map<String, Object> getScenarioContext();

    Map<String, Object> getEvaluatedInputs();

    Map<String, Object> getStepOutputs();

    // TODO Only used by remote step executor
    void addStepOutputs(Map<String, Object> stepOutputs);

    // TODO Only used by remote step executor
    void addScenarioContext(Map<String, Object> context);
}

package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public interface IParseBusinessStep {

    /** TODO put description here **/
    StepDefinition mapToStepDefinition(String environment, Step step, List<StepDefinition> subSteps, StepStrategyDefinition stepStrategyDefinition);

}

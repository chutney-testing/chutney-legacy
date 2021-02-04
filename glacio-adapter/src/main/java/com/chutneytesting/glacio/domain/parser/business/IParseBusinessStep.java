package com.chutneytesting.glacio.domain.parser.business;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public interface IParseBusinessStep {

    /** TODO put description here **/
    StepDefinition mapToStepDefinition(ParsingContext context, Step step, List<StepDefinition> subSteps, StepStrategyDefinition stepStrategyDefinition);

}

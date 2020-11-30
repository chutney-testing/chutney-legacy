package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public class GlacioBusinessStepParser  implements IParseBusinessStep {

    @Override
    public StepDefinition mapToStepDefinition(String environment, Step step, List<StepDefinition> subSteps, StepStrategyDefinition stepStrategyDefinition) {
        return new StepDefinition(
            parseStepName(step),
            null,
            "",
            stepStrategyDefinition,
            emptyMap(),
            subSteps,
            emptyMap(),
            environment
        );
    }

    public String parseStepName(Step step) {
        return step.getText();
    }

}


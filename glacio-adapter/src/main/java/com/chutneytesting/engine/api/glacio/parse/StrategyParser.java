package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;

public interface StrategyParser {
    default StepStrategyDefinition parseStepStrategy(Step step) {
        return null;
    }
}

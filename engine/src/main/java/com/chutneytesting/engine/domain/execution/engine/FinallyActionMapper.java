package com.chutneytesting.engine.domain.execution.engine;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.task.spi.FinallyAction;

class FinallyActionMapper {

    StepDefinition toStepDefinition(FinallyAction finallyAction) {
        return new StepDefinition(
            "Finally action generated for " + finallyAction.originalTask(),
            finallyAction.target()
                .orElse(TargetImpl.NONE),
            finallyAction.actionIdentifier(),
            finallyAction.strategyType()
                .map(st -> new StepStrategyDefinition(st, new StrategyProperties(finallyAction.strategyProperties().orElse(emptyMap()))))
                .orElse(null),
            finallyAction.inputs(),
            null,
            null,
            null,
            null
        );
    }
}

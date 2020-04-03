package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.task.spi.FinallyAction;

class FinallyActionMapper {

    StepDefinition toStepDefinition(FinallyAction finallyAction) {
        return new StepDefinition(
            "Finally action generated",
            finallyAction.target()
                .map(this::mapTarget)
                .orElse(Target.NONE),
            finallyAction.actionIdentifier(),
            null,
            finallyAction.inputs(),
            null,
            null
        );
    }

    private Target mapTarget(com.chutneytesting.task.spi.injectable.Target target) {
        return (Target) target;
    }

}

package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.task.spi.FinallyAction;

class FinallyActionMapper {

    StepDefinition toStepDefinition(FinallyAction finallyAction) {
        return new StepDefinition(
            "Finally action generated",
            finallyAction.target()
                .orElse(TargetImpl.NONE),
            finallyAction.actionIdentifier(),
            null,
            finallyAction.inputs(),
            null,
            null,
            null,
            null
        );
    }
}

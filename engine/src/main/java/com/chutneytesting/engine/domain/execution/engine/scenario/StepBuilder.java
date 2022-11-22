package com.chutneytesting.engine.domain.execution.engine.scenario;

import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.action.spi.FinallyAction;

public interface StepBuilder {

    Step buildStep(FinallyAction definition);
}

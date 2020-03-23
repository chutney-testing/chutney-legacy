package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Step;

public interface GlacioExecutableStepParser {

    Integer priority();
    boolean couldParse(Step step);
    StepDefinition parseStep(Step step);
}

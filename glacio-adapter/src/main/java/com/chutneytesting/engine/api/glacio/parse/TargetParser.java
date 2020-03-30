package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.environment.Target;
import com.github.fridujo.glacio.ast.Step;

public interface TargetParser {
    default Target parseStepTarget(Step step) {
        return null;
    }
}

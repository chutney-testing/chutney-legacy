package com.chutneytesting.engine.api.glacio.parse;

import com.github.fridujo.glacio.model.Step;

public interface StepParser<T> {

    T parseStep(Step step);

    default T parseStepForEnv(String env, Step step) {
        return parseStep(step);
    }

}

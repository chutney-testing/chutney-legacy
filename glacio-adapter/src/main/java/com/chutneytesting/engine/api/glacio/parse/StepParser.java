package com.chutneytesting.engine.api.glacio.parse;

import com.github.fridujo.glacio.ast.Step;

public interface StepParser<T> {
    T parseStep(Step step);
}

package com.chutneytesting.glacio.domain.parser;

import com.github.fridujo.glacio.model.Step;

public interface GlacioStepParser<T> {

    T parseGlacioStep(ParsingContext context, Step step);

}

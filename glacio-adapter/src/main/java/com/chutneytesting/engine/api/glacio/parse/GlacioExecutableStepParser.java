package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface GlacioExecutableStepParser {

    Map<Locale, Set<String>> keywords();
    StepDefinition parseStep(Step step);
}

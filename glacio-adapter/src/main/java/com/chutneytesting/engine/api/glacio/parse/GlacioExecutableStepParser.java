package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface GlacioExecutableStepParser extends StepParser<StepDefinition> {
    Map<Locale, Set<String>> keywords();
}

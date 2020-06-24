package com.chutneytesting.engine.api.glacio.parse.specific;

import com.chutneytesting.engine.api.glacio.parse.default_.StrategyParser;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StrategySoftAssertParser extends StrategyParser {

    @Override
    public Map<Locale, Set<String>> keywords() {
        Map<Locale, Set<String>> keywords = new HashMap<>();
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("soft", "softly")));
        return keywords;
    }

    @Override
    public StepStrategyDefinition toStrategyDef(Locale lang, String parameters) {
        return new StepStrategyDefinition("soft-assert", parseProperties(lang, parameters));
    }

}

package com.chutneytesting.glacio.domain.parser.strategy;

import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StrategySoftAssertParser extends StrategyParser {

    private final Map<Locale, Set<String>> keywords = new HashMap<>(2);

    public StrategySoftAssertParser() {
        keywords.put(Locale.ENGLISH,
            new HashSet<>(Arrays.asList("soft", "softly")));
        keywords.put(Locale.FRENCH,
            new HashSet<>(Arrays.asList("soft", "softly", "doucement")));
    }

    @Override
    public Map<Locale, Set<String>> keywords() {
        return keywords;
    }

    @Override
    public StepStrategyDefinition toStrategyDef(Locale lang, String parameters) {
        return new StepStrategyDefinition("soft-assert", parseProperties(lang, parameters));
    }

}

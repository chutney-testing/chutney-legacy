package com.chutneytesting.glacio.domain.parser.strategy;

import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public interface IParseStrategy {

    Map<Locale, Set<String>> keywords();

    List<StepStrategyDefinition> parseGlacioStep(Locale lang, Step step);

    Pair<Step, List<StepStrategyDefinition>> parseStepAndStripStrategy(Locale lang, Step step);

    default List<StepStrategyDefinition> parseGlacioStep(Step step) {
        return parseGlacioStep(Locale.ENGLISH, step);
    }

}

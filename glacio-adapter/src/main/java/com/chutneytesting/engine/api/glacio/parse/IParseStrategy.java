package com.chutneytesting.engine.api.glacio.parse;

import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface IParseStrategy extends StepParser<List<StepStrategyDefinition>> {

    Map<Locale, Set<String>> keywords();

    List<StepStrategyDefinition> parseStep(Locale lang, Step step);

    default List<StepStrategyDefinition> parseStep(Step step) {
        return parseStep(Locale.ENGLISH, step);
    }

}

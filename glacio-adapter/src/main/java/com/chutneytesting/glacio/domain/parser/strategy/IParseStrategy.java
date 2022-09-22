package com.chutneytesting.glacio.domain.parser.strategy;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public interface IParseStrategy {

    Map<Locale, Set<String>> keywords();

    List<StepDefinitionDto.StepStrategyDefinitionDto> parseGlacioStep(Locale lang, Step step);

    Pair<Step, List<StepDefinitionDto.StepStrategyDefinitionDto>> parseStepAndStripStrategy(Locale lang, Step step);

    default List<StepDefinitionDto.StepStrategyDefinitionDto> parseGlacioStep(Step step) {
        return parseGlacioStep(Locale.ENGLISH, step);
    }

}

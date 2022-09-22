package com.chutneytesting.glacio.domain.parser.strategy;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class NoStrategyParser implements IParseStrategy {

    @Override
    public Map<Locale, Set<String>> keywords() {
        return emptyMap();
    }

    @Override
    public List<StepDefinitionDto.StepStrategyDefinitionDto> parseGlacioStep(Locale lang, Step step) {
        return emptyList();
    }

    @Override
    public Pair<Step, List<StepDefinitionDto.StepStrategyDefinitionDto>> parseStepAndStripStrategy(Locale lang, Step step) {
        return Pair.of(step, emptyList());
    }
}

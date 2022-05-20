package com.chutneytesting.glacio.domain.parser.business;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public class BusinessGlacioStepParser implements IParseBusinessStep {

    @Override
    public StepDefinitionDto mapToStepDefinition(ParsingContext context, Step step, List<StepDefinitionDto> subSteps, StepDefinitionDto.StepStrategyDefinitionDto stepStrategyDefinition) {
        return new StepDefinitionDto(
            parseStepName(step),
            null,
            "",
            stepStrategyDefinition,
            emptyMap(),
            subSteps,
            emptyMap(),
            emptyMap()
        );
    }

    public String parseStepName(Step step) {
        return step.getText();
    }

}


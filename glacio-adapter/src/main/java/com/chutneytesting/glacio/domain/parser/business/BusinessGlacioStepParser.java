package com.chutneytesting.glacio.domain.parser.business;

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public class BusinessGlacioStepParser implements IParseBusinessStep {

    @Override
    public StepDefinition mapToStepDefinition(ParsingContext context, Step step, List<StepDefinition> subSteps, StepStrategyDefinition stepStrategyDefinition) {
        return new StepDefinition(
            parseStepName(step),
            null,
            "",
            stepStrategyDefinition,
            emptyMap(),
            subSteps,
            emptyMap(),
            context.values.get(ENVIRONMENT)
        );
    }

    public String parseStepName(Step step) {
        return step.getText();
    }

}


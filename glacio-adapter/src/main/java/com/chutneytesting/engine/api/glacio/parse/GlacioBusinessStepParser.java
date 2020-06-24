package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;

public class GlacioBusinessStepParser  implements IParseBusinessStep {

    protected IParseStrategy strategyParser;

    public GlacioBusinessStepParser(IParseStrategy strategyParser) {
        this.strategyParser = strategyParser;
    }

    @Override
    public StepDefinition mapToStepDefinition(Locale lang, String environment, Step step, List<StepDefinition> subSteps) {
        return new StepDefinition(
            parseStepName(step),
            null,
            "",
            parseStepStrategy(lang, step),
            emptyMap(),
            subSteps,
            emptyMap(),
            environment
        );
    }

    public String parseStepName(Step step) {
        return step.getText();
    }

    private StepStrategyDefinition parseStepStrategy(Locale lang, Step step) {
        List<StepStrategyDefinition> stepStrategyDefinitions = strategyParser.parseStep(lang, step);
        if (stepStrategyDefinitions.size() > 0) {
            return stepStrategyDefinitions.get(0);
        }

        return null;
    }

}


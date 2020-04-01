package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public abstract class GlacioParser implements GlacioExecutableStepParser {

    protected StepParser<Target> targetParser;
    protected StepParser<Map<String, Object>> inputsParser;
    protected StepParser<Map<String, Object>> outputsParser;
    protected StepParser<StepStrategyDefinition> strategyParser;

    @Override
    public final StepDefinition parseStep(Step step) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(step),
            parseTaskType(step),
            parseStepStrategy(step),
            parseTaskInputs(step),
            emptyList(),
            parseTaskOutputs(step)
        );
    }

    public abstract String parseTaskType(Step step);

    public String parseStepName(Step step) {
        return step.getText();
    }

    private Map<String, Object> parseTaskInputs(Step step) {
        return inputsParser.parseStep(step);
    }

    private Map<String, Object> parseTaskOutputs(Step step) {
        return outputsParser.parseStep(step);
    }

    private Target parseStepTarget(Step step) {
        return targetParser.parseStep(step);
    }

    private StepStrategyDefinition parseStepStrategy(Step step) {
        return strategyParser.parseStep(step);
    }

}

package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public abstract class GlacioParser implements GlacioExecutableStepParser {

    protected TargetParser targetParser;
    protected InputsParser inputsParser;
    protected OutputsParser outputsParser;
    protected StrategyParser strategyParser;

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
        return inputsParser.parseTaskInputs(step);
    }

    private Map<String, Object> parseTaskOutputs(Step step) {
        return outputsParser.parseTaskOutputs(step);
    }

    private Target parseStepTarget(Step step) {
        return targetParser.parseStepTarget(step);
    }

    private StepStrategyDefinition parseStepStrategy(Step step) {
        return strategyParser.parseStepStrategy(step);
    }

}

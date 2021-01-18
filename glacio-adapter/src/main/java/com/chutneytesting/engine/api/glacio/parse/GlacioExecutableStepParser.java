package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;

public abstract class GlacioExecutableStepParser implements IParseExecutableStep {

    protected StepParser<Target> targetParser;
    protected StepParser<Map<String, Object>> inputsParser;
    protected StepParser<Map<String, Object>> outputsParser;

    public GlacioExecutableStepParser(StepParser<Target> targetParser,
                                      StepParser<Map<String, Object>> inputsParser,
                                      StepParser<Map<String, Object>> outputsParser) {
        this.targetParser = targetParser;
        this.inputsParser = inputsParser;
        this.outputsParser = outputsParser;
    }

    @Override
    public final StepDefinition mapToStepDefinition(String environment, Step step, StepStrategyDefinition stepStrategyDefinition) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(environment, step),
            parseTaskType(step),
            stepStrategyDefinition,
            parseTaskInputs(step),
            emptyList(),
            parseTaskOutputs(step),
            environment
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

    private Target parseStepTarget(String environment, Step step) {
        return targetParser.parseStepForEnv(environment, step);
    }

}

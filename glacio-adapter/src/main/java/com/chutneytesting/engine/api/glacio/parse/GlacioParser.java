package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.ast.Step;
import java.util.Map;

public abstract class GlacioParser implements GlacioExecutableStepParser {

    protected StepParser<Target> targetParser;
    protected StepParser<Map<String, Object>> inputsParser;
    protected StepParser<Map<String, Object>> outputsParser;
    protected StepParser<StepStrategyDefinition> strategyParser;

    public GlacioParser(StepParser<Target> targetParser,
                        StepParser<Map<String, Object>> inputsParser,
                        StepParser<Map<String, Object>> outputsParser,
                        StepParser<StepStrategyDefinition> strategyParser) {
        this.targetParser = targetParser;
        this.inputsParser = inputsParser;
        this.outputsParser = outputsParser;
        this.strategyParser = strategyParser;
    }

    @Override
    public final StepDefinition mapToStepDefinition(String environment, Step step) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(step),
            parseTaskType(step),
            parseStepStrategy(step),
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

    private Target parseStepTarget(Step step) {
        return targetParser.parseStep(step);
    }

    private StepStrategyDefinition parseStepStrategy(Step step) {
        return strategyParser.parseStep(step);
    }

}

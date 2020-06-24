package com.chutneytesting.engine.api.glacio.parse;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class GlacioExecutableStepParser implements IParseExecutableStep {

    protected StepParser<Target> targetParser;
    protected StepParser<Map<String, Object>> inputsParser;
    protected StepParser<Map<String, Object>> outputsParser;
    protected IParseStrategy strategyParser;

    public GlacioExecutableStepParser(StepParser<Target> targetParser,
                                      StepParser<Map<String, Object>> inputsParser,
                                      StepParser<Map<String, Object>> outputsParser,
                                      IParseStrategy strategyParser) {
        this.targetParser = targetParser;
        this.inputsParser = inputsParser;
        this.outputsParser = outputsParser;
        this.strategyParser = strategyParser;
    }

    @Override
    public final StepDefinition mapToStepDefinition(Locale lang, String environment, Step step) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(environment, step),
            parseTaskType(step),
            parseStepStrategy(lang, step),
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

    private StepStrategyDefinition parseStepStrategy(Locale lang, Step step) {
        List<StepStrategyDefinition> stepStrategyDefinitions = strategyParser.parseStep(lang, step);
        if (stepStrategyDefinitions.size() > 0) {
            return stepStrategyDefinitions.get(0);
        }

        return null;
    }

}

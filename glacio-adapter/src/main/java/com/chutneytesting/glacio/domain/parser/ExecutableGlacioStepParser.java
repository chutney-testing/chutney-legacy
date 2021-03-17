package com.chutneytesting.glacio.domain.parser;

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;

public abstract class ExecutableGlacioStepParser implements IParseExecutableStep {

    protected final GlacioStepParser<Target> targetParser;
    protected final GlacioStepParser<Map<String, Object>> inputsParser;
    protected final GlacioStepParser<Map<String, Object>> outputsParser;
    protected final GlacioStepParser<Map<String, Object>> validationsParser;

    public ExecutableGlacioStepParser(GlacioStepParser<Target> targetParser,
                                      GlacioStepParser<Map<String, Object>> inputsParser,
                                      GlacioStepParser<Map<String, Object>> outputsParser,
                                      GlacioStepParser<Map<String, Object>> validationsParser) {
        this.targetParser = targetParser;
        this.inputsParser = inputsParser;
        this.outputsParser = outputsParser;
        this.validationsParser = validationsParser;
    }

    public abstract String parseTaskType(Step step);

    @Override
    public final StepDefinition mapToStepDefinition(ParsingContext context, Step step, StepStrategyDefinition stepStrategyDefinition) {
        return new StepDefinition(
            parseStepName(step),
            parseStepTarget(context, step),
            parseTaskType(step),
            stepStrategyDefinition,
            parseTaskInputs(context, step),
            emptyList(),
            parseTaskOutputs(context, step),
            parseTaskValidations(context, step),
            context.values.get(ENVIRONMENT)
        );
    }

    private String parseStepName(Step step) {
        return step.getText();
    }

    private Map<String, Object> parseTaskInputs(ParsingContext context, Step step) {
        return inputsParser.parseGlacioStep(context, step);
    }

    private Map<String, Object> parseTaskOutputs(ParsingContext context, Step step) {
        return outputsParser.parseGlacioStep(context, step);
    }

    private Map<String, Object> parseTaskValidations(ParsingContext context, Step step) {
        return validationsParser.parseGlacioStep(context, step);
    }

    private Target parseStepTarget(ParsingContext context, Step step) {
        return targetParser.parseGlacioStep(context, step);
    }

}

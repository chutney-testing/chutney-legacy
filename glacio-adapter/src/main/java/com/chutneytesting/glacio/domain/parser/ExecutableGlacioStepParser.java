package com.chutneytesting.glacio.domain.parser;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.github.fridujo.glacio.model.Step;
import java.util.Map;

public abstract class ExecutableGlacioStepParser implements IParseExecutableStep {

    protected final GlacioStepParser<TargetExecutionDto> targetParser;
    protected final GlacioStepParser<Map<String, Object>> inputsParser;
    protected final GlacioStepParser<Map<String, Object>> outputsParser;
    protected final GlacioStepParser<Map<String, Object>> validationsParser;

    public ExecutableGlacioStepParser(GlacioStepParser<TargetExecutionDto> targetParser,
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
    public final StepDefinitionDto mapToStepDefinition(ParsingContext context, Step step, StepDefinitionDto.StepStrategyDefinitionDto stepStrategyDefinition) {
        return new StepDefinitionDto(
            parseStepName(step),
            parseStepTarget(context, step),
            parseTaskType(step),
            stepStrategyDefinition,
            parseTaskInputs(context, step),
            emptyList(),
            parseTaskOutputs(context, step),
            parseTaskValidations(context, step)
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

    private TargetExecutionDto parseStepTarget(ParsingContext context, Step step) {
        return targetParser.parseGlacioStep(context, step);
    }

}

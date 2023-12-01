/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public abstract String parseActionType(Step step);

    @Override
    public final StepDefinitionDto mapToStepDefinition(ParsingContext context, Step step, StepDefinitionDto.StepStrategyDefinitionDto stepStrategyDefinition) {
        return new StepDefinitionDto(
            parseStepName(step),
            parseStepTarget(context, step),
            parseActionType(step),
            stepStrategyDefinition,
            parseActionInputs(context, step),
            emptyList(),
            parseActionOutputs(context, step),
            parseActionValidations(context, step)
        );
    }

    private String parseStepName(Step step) {
        return step.getText();
    }

    private Map<String, Object> parseActionInputs(ParsingContext context, Step step) {
        return inputsParser.parseGlacioStep(context, step);
    }

    private Map<String, Object> parseActionOutputs(ParsingContext context, Step step) {
        return outputsParser.parseGlacioStep(context, step);
    }

    private Map<String, Object> parseActionValidations(ParsingContext context, Step step) {
        return validationsParser.parseGlacioStep(context, step);
    }

    private TargetExecutionDto parseStepTarget(ParsingContext context, Step step) {
        return targetParser.parseGlacioStep(context, step);
    }

}

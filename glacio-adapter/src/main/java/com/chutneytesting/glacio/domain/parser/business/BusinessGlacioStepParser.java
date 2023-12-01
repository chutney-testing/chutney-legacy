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

package com.chutneytesting.glacio.domain.parser.business;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.github.fridujo.glacio.model.Step;
import java.util.List;

public class BusinessGlacioStepParser implements IParseBusinessStep {

    @Override
    public StepDefinitionDto mapToStepDefinition(ParsingContext context, Step step, List<StepDefinitionDto> subSteps, StepDefinitionDto.StepStrategyDefinitionDto stepStrategyDefinition) {
        return new StepDefinitionDto(
            parseStepName(step),
            null,
            "",
            stepStrategyDefinition,
            emptyMap(),
            subSteps,
            emptyMap(),
            emptyMap()
        );
    }

    public String parseStepName(Step step) {
        return step.getText();
    }

}


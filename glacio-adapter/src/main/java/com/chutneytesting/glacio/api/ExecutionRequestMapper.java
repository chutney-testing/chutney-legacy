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

package com.chutneytesting.glacio.api;

import com.chutneytesting.engine.api.execution.DatasetDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutionRequestMapper {

    public static ExecutionRequestDto toDto(StepDefinitionDto stepDefinitionDto, String environment) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(stepDefinitionDto);
        return new ExecutionRequestDto(stepDefinitionRequestDto, environment, new DatasetDto(Collections.emptyMap(), Collections.emptyList()));
    }

    private static StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(StepDefinitionDto definition) {
        final ExecutionRequestDto.StepStrategyDefinitionRequestDto strategy;
        if (definition.strategy != null) {
            strategy = new ExecutionRequestDto.StepStrategyDefinitionRequestDto(definition.strategy.type, definition.strategy.strategyProperties);
        } else {
            strategy = null;
        }

        List<StepDefinitionRequestDto> steps = definition.steps.stream()
            .map(ExecutionRequestMapper::getStepDefinitionRequestFromStepDef)
            .collect(Collectors.toList());

        return new StepDefinitionRequestDto(
            definition.name,
            definition.getTarget().orElse(null),
            strategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs,
            definition.validations
        );
    }

}

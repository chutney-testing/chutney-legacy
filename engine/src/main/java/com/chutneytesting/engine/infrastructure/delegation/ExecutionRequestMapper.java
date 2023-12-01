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

package com.chutneytesting.engine.infrastructure.delegation;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepStrategyDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.Dataset;
import java.util.List;
import java.util.stream.Collectors;

class ExecutionRequestMapper {

    static ExecutionRequestDto from(StepDefinition stepDefinition, Dataset dataset) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(stepDefinition);
        return new ExecutionRequestDto(stepDefinitionRequestDto, stepDefinition.environment, DatasetMapper.toDto(dataset));
    }

    private static StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(StepDefinition definition) {
        final StepStrategyDefinitionRequestDto strategy = definition.getStrategy()
            .map(s -> new StepStrategyDefinitionRequestDto(
                definition.getStrategy().get().type,
                definition.getStrategy().get().strategyProperties)
            ).orElse(null);

        List<StepDefinitionRequestDto> steps = definition.steps.stream()
            .map(ExecutionRequestMapper::getStepDefinitionRequestFromStepDef)
            .collect(Collectors.toList());

        return new StepDefinitionRequestDto(
            definition.name,
            extractTarget(definition),
            strategy,
            definition.type,
            definition.inputs(),
            steps,
            definition.outputs,
            definition.validations);
    }

    private static TargetExecutionDto extractTarget(StepDefinition definition) {
        return definition.getTarget().map(t -> (TargetImpl) t).map(t -> new TargetExecutionDto(
                t.name(),
                t.uri().toString(),
                t.properties,
                t.agents
            ))
            .orElse(null);
    }
}

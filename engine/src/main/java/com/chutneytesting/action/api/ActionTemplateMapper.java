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

package com.chutneytesting.action.api;

import static java.util.stream.Collectors.partitioningBy;

import com.chutneytesting.action.api.ActionDto.InputsDto;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.List;
import java.util.Map;

public class ActionTemplateMapper {

    private ActionTemplateMapper() {
    }

    public static ActionDto toDto(ActionTemplate actionTemplate) {
        return new ActionDto(actionTemplate.identifier(),
            hasTarget(actionTemplate),
            toInputsDto(actionTemplate)
        );
    }

    private static boolean hasTarget(ActionTemplate actionTemplate) {
        return actionTemplate.parameters().stream().anyMatch(p -> p.rawType().equals(Target.class));
    }

    private static List<InputsDto> toInputsDto(ActionTemplate actionTemplate) {
       return actionTemplate.parameters().stream()
            .filter(parameter -> parameter.annotations().optional(Input.class).isPresent())
            .map(ActionTemplateMapper::simpleParameterToInputsDto)
            .toList();
    }

    private static InputsDto simpleParameterToInputsDto(Parameter parameter) {
        return new InputsDto(parameter.annotations().get(Input.class).value(), parameter.rawType());
    }
}

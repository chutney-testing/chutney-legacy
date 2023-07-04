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
        Map<Boolean, List<Parameter>> parametersMap = actionTemplate.parameters().stream()
            .filter(parameter -> parameter.annotations().optional(Input.class).isPresent())
            .collect(partitioningBy(ActionTemplateMapper::isSimpleType));

        return parametersMap.get(true).stream().map(ActionTemplateMapper::simpleParameterToInputsDto).toList();
    }

    private static InputsDto simpleParameterToInputsDto(Parameter parameter) {
        return new InputsDto(parameter.annotations().get(Input.class).value(), parameter.rawType());
    }


    private static boolean isSimpleType(Parameter parameter) {
        Class<?> rawType = parameter.rawType();
        return rawType.isPrimitive() || rawType.equals(String.class) || rawType.equals(Object.class);
    }
}

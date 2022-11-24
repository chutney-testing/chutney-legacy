package com.chutneytesting.action.api;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.action.api.ActionDto.InputsDto;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

        return Stream.concat(
            parametersMap.get(true).stream()
                .map(ActionTemplateMapper::simpleParameterToInputsDto),
            parametersMap.get(false).stream()
                .map(ActionTemplateMapper::complexParameterToInputsDto)
                .flatMap(Collection::stream))
            .collect(toList());
    }

    private static InputsDto simpleParameterToInputsDto(Parameter parameter) {
        return new InputsDto(parameter.annotations().get(Input.class).value(), parameter.rawType());
    }

    private static List<InputsDto> complexParameterToInputsDto(Parameter parameter) {
        Constructor<?>[] constructors = parameter.rawType().getConstructors();
        if (constructors.length == 1) {
            Constructor constructor = constructors[0];
            List<InputsDto> result = Arrays.stream(constructor.getParameters())
                .map(Parameter::fromJavaParameter)
                .filter(p -> p.annotations().optional(Input.class).isPresent())
                .map(p -> new InputsDto(p.annotations().get(Input.class).value(), p.rawType()))
                .collect(toList());
            if (!result.isEmpty()) {
                return result;
            }
        }
        return singletonList(simpleParameterToInputsDto(parameter));
    }

    private static boolean isSimpleType(Parameter parameter) {
        Class<?> rawType = parameter.rawType();
        return rawType.isPrimitive() || rawType.equals(String.class) || rawType.equals(Object.class);
    }
}

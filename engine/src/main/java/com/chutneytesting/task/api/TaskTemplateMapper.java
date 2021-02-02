package com.chutneytesting.task.api;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.task.api.TaskDto.InputsDto;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TaskTemplateMapper {

    private TaskTemplateMapper() {
    }

    public static TaskDto toDto(TaskTemplate taskTemplate) {
        return new TaskDto(taskTemplate.identifier(),
            hasTarget(taskTemplate),
            toInputsDto(taskTemplate)
        );
    }

    private static boolean hasTarget(TaskTemplate taskTemplate) {
        return taskTemplate.parameters().stream().anyMatch(p -> p.rawType().equals(Target.class));
    }

    private static List<InputsDto> toInputsDto(TaskTemplate taskTemplate) {
        Map<Boolean, List<Parameter>> parametersMap = taskTemplate.parameters().stream()
            .filter(parameter -> parameter.annotations().optional(Input.class).isPresent())
            .collect(partitioningBy(TaskTemplateMapper::isSimpleType));

        return Stream.concat(
            parametersMap.get(true).stream()
                .map(TaskTemplateMapper::simpleParameterToInputsDto),
            parametersMap.get(false).stream()
                .map(TaskTemplateMapper::complexParameterToInputsDto)
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
            return Arrays.stream(constructor.getParameters())
                .map(Parameter::fromJavaParameter)
                .filter(p -> p.annotations().optional(Input.class).isPresent())
                .map(p -> new InputsDto(p.annotations().get(Input.class).value(), p.rawType()))
                .collect(toList());
        } else {
            return singletonList(simpleParameterToInputsDto(parameter));
        }
    }

    private static boolean isSimpleType(Parameter parameter) {
        Class<?> rawType = parameter.rawType();
        return rawType.isPrimitive() || rawType.equals(String.class) || rawType.equals(Object.class);
    }
}

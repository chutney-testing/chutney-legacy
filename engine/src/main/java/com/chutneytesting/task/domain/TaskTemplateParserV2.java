package com.chutneytesting.task.domain;

import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.spi.Task;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskTemplateParserV2 implements TaskTemplateParser<Task> {
    private static final String CLASS_NAME_TASK_SUFFIX = "Task";
    private static final Converter<String, String> CAMEL_TO_HYPHEN_CONVERTER = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);

    @Override
    public ResultOrError<TaskTemplate, ParsingError> parse(Class<? extends Task> taskClass) {
        if (taskClass.getDeclaredConstructors().length > 1) {
            return ResultOrError.error(new ParsingError(taskClass, "More than one constructor"));
        }
        String taskName = computeTaskName(taskClass);
        Constructor<? extends Task> constructor = (Constructor<? extends Task>) taskClass.getDeclaredConstructors()[0];
        List<Parameter> parameters = extractParameters(constructor);
        return ResultOrError.result(new TaskTemplateV2(taskName, taskClass, constructor, parameters));
    }

    private List<Parameter> extractParameters(Constructor<? extends Task> constructor) {
        return Arrays.stream(constructor.getParameters())
            .map(Parameter::fromJavaParameter)
            .collect(Collectors.toList());
    }

    private String computeTaskName(Class<? extends Task> taskClass) {
        // TODO extract @taskIdentifier if present
        String taskName = taskClass.getSimpleName();
        if (taskName.endsWith(CLASS_NAME_TASK_SUFFIX)) {
            taskName = taskName.substring(0, taskName.length() - CLASS_NAME_TASK_SUFFIX.length());
        }
        taskName = CAMEL_TO_HYPHEN_CONVERTER.convert(taskName);
        return taskName;
    }
}

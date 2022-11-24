package com.chutneytesting.action.domain;

import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.Action;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionTemplateParserV2 implements ActionTemplateParser<Action> {
    private static final String CLASS_NAME_ACTION_SUFFIX = "Action";
    private static final Converter<String, String> CAMEL_TO_HYPHEN_CONVERTER = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);

    @Override
    public ResultOrError<ActionTemplate, ParsingError> parse(Class<? extends Action> taskClass) {
        if (taskClass.getDeclaredConstructors().length > 1) {
            return ResultOrError.error(new ParsingError(taskClass, "More than one constructor"));
        }
        String taskName = computeActionName(taskClass);
        Constructor<? extends Action> constructor = (Constructor<? extends Action>) taskClass.getDeclaredConstructors()[0];
        List<Parameter> parameters = extractParameters(constructor);
        return ResultOrError.result(new ActionTemplateV2(taskName, taskClass, constructor, parameters));
    }

    private List<Parameter> extractParameters(Constructor<? extends Action> constructor) {
        return Arrays.stream(constructor.getParameters())
            .map(Parameter::fromJavaParameter)
            .collect(Collectors.toList());
    }

    private String computeActionName(Class<? extends Action> taskClass) {
        // TODO extract @taskIdentifier if present
        String taskName = taskClass.getSimpleName();
        if (taskName.endsWith(CLASS_NAME_ACTION_SUFFIX)) {
            taskName = taskName.substring(0, taskName.length() - CLASS_NAME_ACTION_SUFFIX.length());
        }
        taskName = CAMEL_TO_HYPHEN_CONVERTER.convert(taskName);
        return taskName;
    }
}

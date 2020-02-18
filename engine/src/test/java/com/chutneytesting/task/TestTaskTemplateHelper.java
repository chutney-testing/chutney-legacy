package com.chutneytesting.task;

import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.parameter.AnnotationSet;
import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.spi.injectable.Input;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mockito;

public class TestTaskTemplateHelper {
    private TestTaskTemplateHelper() {
    }

    public static TaskTemplate mockTaskTemplate(String identifier, Set<Parameter> parameters) {
        TaskTemplate taskTemplate = Mockito.mock(TaskTemplate.class);
        Mockito.when(taskTemplate.identifier()).thenReturn(identifier);
        Mockito.when(taskTemplate.parameters()).thenReturn(parameters);
        return taskTemplate;
    }

    public static Parameter mockParameter(Class type) {
        return mockParameter(type, null);
    }

    public static Parameter mockParameter(Class type, String inputValue) {
        Parameter parameter = Mockito.mock(Parameter.class);
        Mockito.when(parameter.rawType()).thenReturn(type);
        if (inputValue != null) {
            Input newInput = new Input() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Input.class;
                }

                @Override
                public String value() {
                    return inputValue;
                }
            };
            Mockito.when(parameter.annotations()).thenReturn(new AnnotationSet(new HashSet<Annotation>(Collections.singletonList(newInput))));
        } else {
            Mockito.when(parameter.annotations()).thenReturn(new AnnotationSet(Collections.emptySet()));
        }
        return parameter;
    }
}

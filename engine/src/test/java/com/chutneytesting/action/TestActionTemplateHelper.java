package com.chutneytesting.action;

import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.parameter.AnnotationSet;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.injectable.Input;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mockito;

public class TestActionTemplateHelper {
    private TestActionTemplateHelper() {
    }

    public static ActionTemplate mockActionTemplate(String identifier, Set<Parameter> parameters) {
        ActionTemplate actionTemplate = Mockito.mock(ActionTemplate.class);
        Mockito.when(actionTemplate.identifier()).thenReturn(identifier);
        Mockito.when(actionTemplate.parameters()).thenReturn(parameters);
        return actionTemplate;
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
            Mockito.when(parameter.annotations()).thenReturn(new AnnotationSet(new HashSet<>(Collections.singletonList(newInput))));
        } else {
            Mockito.when(parameter.annotations()).thenReturn(new AnnotationSet(Collections.emptySet()));
        }
        return parameter;
    }
}

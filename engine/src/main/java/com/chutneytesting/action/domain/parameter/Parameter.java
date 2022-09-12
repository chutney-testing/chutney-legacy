package com.chutneytesting.action.domain.parameter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * An action parameter description.
 */
public class Parameter {

    private final String name;
    private final AnnotationSet annotations;
    private final Class<?> rawType;

    private Parameter(String name, AnnotationSet annotations, Class<?> rawType) {
        this.name = name;
        this.annotations = annotations;
        this.rawType = rawType;
    }

    public AnnotationSet annotations() {
        return annotations;
    }

    public Class<?> rawType() {
        return rawType;
    }

    @Override
    public String toString() {
        return "Parameter[" +
            "name=" + name +
            ", rawType=" + rawType +
            ']';
    }

    public static Parameter fromJavaParameter(java.lang.reflect.Parameter parameter) {
        String name = Optional.of(parameter.getName()).filter(s -> parameter.isNamePresent()).orElse("<no name>");
        Set<Annotation> annotations = new LinkedHashSet<>(Arrays.asList(parameter.getDeclaredAnnotations()));
        return new Parameter(name, new AnnotationSet(annotations), parameter.getType());
    }
}

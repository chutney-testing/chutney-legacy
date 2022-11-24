package com.chutneytesting.action.domain.parameter;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public class AnnotationSet {

    private final Set<Annotation> annotations;

    public AnnotationSet(Set<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Optional<Annotation> optional(Class<? extends Annotation> annotationType) {
        return annotations.stream().filter(a -> a.annotationType().equals(annotationType)).findFirst();
    }

    public <T extends Annotation> T get(Class<T> annotationType) {
        // TODO no call to isPresent ??
        return (T) optional(annotationType).get();
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }
}

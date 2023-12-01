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

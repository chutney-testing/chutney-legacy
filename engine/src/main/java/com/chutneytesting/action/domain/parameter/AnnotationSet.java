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

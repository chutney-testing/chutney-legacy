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

package com.chutneytesting.engine.domain.execution.evaluation;

import com.chutneytesting.action.spi.SpelFunction;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.springframework.util.ReflectionUtils;

/**
 * Spring ReflectionUtils.MethodCallback use to find all method from specified class with @{@link SpelFunction} annotation.
 *
 * This class produce a SpelFunctions who contains all Method mark with {@link SpelFunction} declared on class from
 * ReflectionUtils.doWithMethods
 */
public class SpelFunctionCallback implements ReflectionUtils.MethodCallback {
    private final SpelFunctions spelFunctions = new SpelFunctions();

    @Override
    public void doWith(Method method) throws IllegalArgumentException {
        if (!method.isAnnotationPresent(SpelFunction.class)) {
            return;
        }
        SpelFunction spelFunction = method.getDeclaredAnnotation(SpelFunction.class);
        if(!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("Given function " + spelFunction.value() + " (" + method.getName() + ") must match a static method");
        }
        spelFunctions.add(spelFunction, method);
    }

    public SpelFunctions getSpelFunctions() {
        return spelFunctions;
    }
}

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All find {@link SpelFunction} Method annotated, this bean should be use to register function on Spring Epel context with
 *
 * {@link org.springframework.expression.spel.support.StandardEvaluationContext#registerFunction(String, Method)}
 *
 * Default name is original method name retrieved with {@link Method#getName()}
 */
public class SpelFunctions {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpelFunctions.class);
    private final Map<String, NamedFunctionLink> methodByName = new HashMap<>();

    void add(SpelFunction spelFunction, Method method) {
        String spelFunctionName = Optional.of(spelFunction.value())
                                            .filter(s -> !s.isEmpty() )
                                            .orElse(method.getName());
        NamedFunctionLink previousDeclaration;
        if( (previousDeclaration = methodByName.put(spelFunctionName, new NamedFunctionLink(spelFunctionName, method))) != null) {
            LOGGER.error("Loading function conflicting: {} ({}) with previous declaration {} ({})",
                spelFunctionName,
                method.getDeclaringClass().getSimpleName(),
                previousDeclaration.getName(),
                previousDeclaration.getMethod().getDeclaringClass().getSimpleName());
        } else {
            LOGGER.debug("Loading function: {} ({})", spelFunctionName, method.getDeclaringClass().getSimpleName());
        }
    }

    public Stream<NamedFunctionLink> stream() {
        return methodByName.values().stream();
    }

    public class NamedFunctionLink {
        private final String name;
        private final Method method;

        public NamedFunctionLink(String name, Method method) {
            this.name = name;
            this.method = method;
        }

        public String getName() {
            return name;
        }

        public Method getMethod() {
            return method;
        }
    }

}


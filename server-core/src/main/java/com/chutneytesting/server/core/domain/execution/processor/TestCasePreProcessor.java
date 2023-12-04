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

package com.chutneytesting.server.core.domain.execution.processor;

import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.scenario.TestCase;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

public interface TestCasePreProcessor<T extends TestCase> {

    T apply(ExecutionRequest executionRequest);

    default boolean test(T testCase) {
        Type type = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return ((Class<?>) type).isAssignableFrom(testCase.getClass());
    }

    default String replaceParams(Map<String, String> dataSet, String concreteString, Function<String, String> escapeValueFunction) {
        String stringReplaced = concreteString;
        for (Map.Entry<String, String> entry : dataSet.entrySet()) {
            String stringToReplace = "**" + entry.getKey() + "**";
            if (stringReplaced.contains(stringToReplace)) {
                stringReplaced = stringReplaced.replace(stringToReplace, escapeValueFunction.apply(entry.getValue()));
            }
        }
        return stringReplaced;
    }
}

package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.chutneytesting.execution.domain.ExecutionRequest;
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

    default String replaceParams(String parameterizedString, Map<String, String> globalDataSet, Map<String, String> dataSet) {
        String concreteString = replaceParams(dataSet, parameterizedString, Function.identity());
        return replaceParams(globalDataSet, concreteString, Function.identity());
    }

    default String replaceParams(String parameterizedString, Map<String, String> globalDataSet, Map<String, String> dataSet, Function<String, String> escapeValueFunction) {
        String concreteString = replaceParams(dataSet, parameterizedString, escapeValueFunction);
        return replaceParams(globalDataSet, concreteString, escapeValueFunction);
    }

    default String replaceParams(Map<String, String> dataSet, String concreteString, Function<String, String> escapeValueFunction) {
        String stringReplaced = concreteString;
        for (Map.Entry<String, String> entry : dataSet.entrySet()) {
            stringReplaced = stringReplaced.replace("**" + entry.getKey() + "**", escapeValueFunction.apply(entry.getValue()));
        }
        return stringReplaced;
    }

}

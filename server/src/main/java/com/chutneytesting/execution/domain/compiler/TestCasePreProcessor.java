package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.TestCase;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;

public interface TestCasePreProcessor<T extends TestCase> {

    T apply(T testCase);

    int order();

    default boolean test(T testCase) {
        Type type = ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return ((Class<?>) type).isAssignableFrom(testCase.getClass());
    }

    default String replaceParams(String parameterizedString, Map<String, String> globalDataSet, Map<String, String> dataSet) {
        String concreteString = parameterizedString;
        concreteString = replaceParams(dataSet, concreteString);
        return replaceParams(globalDataSet, concreteString);
    }

    default String replaceParams(Map<String, String> dataSet, String concreteString) {
        for (Map.Entry<String, String> entry : dataSet.entrySet()) {
            concreteString = concreteString.replace("**" + entry.getKey() + "**", StringEscapeUtils.escapeJson(entry.getValue()));
        }
        return concreteString;
    }

    class PreProcessorComparator implements Comparator<TestCasePreProcessor> {
        public int compare(TestCasePreProcessor a, TestCasePreProcessor b) {
            return b.order() - a.order();
        }
    }
}

package com.chutneytesting.design.domain.scenario;

import java.util.Map;

public interface TestCase {

    TestCaseMetadata metadata();

    default String id() {
        return metadata().id();
    }

    Map<String, String> computedParameters();

    TestCase withDataSet(final Map<String, String> dataSet);

}

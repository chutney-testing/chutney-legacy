package com.chutneytesting.execution.domain;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.design.domain.scenario.TestCase;
import java.util.Map;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;
    public final String userId;
    public final boolean withExternalDataset;

    public ExecutionRequest(TestCase testCase, String environment, boolean withExternalDataset, String userId) {
        this.testCase = testCase;
        this.environment = environment;
        this.withExternalDataset = withExternalDataset;
        this.userId = userId;
    }

    public ExecutionRequest(TestCase testCase, String environment, String userId) {
        this(testCase, environment, false, userId);
    }

    public ExecutionRequest(TestCase testCase, String environment, Map<String, String> specificDataSet, String userId) {
        this(requireNonNull(testCase).usingExecutionParameters(requireNonNull(specificDataSet)), environment, userId);
    }
}

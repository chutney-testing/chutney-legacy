package com.chutneytesting.server.core.execution;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.server.core.scenario.TestCase;
import java.util.Map;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;
    public final String userId;

    public ExecutionRequest(TestCase testCase, String environment, String userId) {
        this.testCase = testCase;
        this.environment = environment;
        this.userId = userId;
    }

    public ExecutionRequest(TestCase testCase, String environment, Map<String, String> specificDataSet, String userId) {
        this(requireNonNull(testCase).usingExecutionParameters(requireNonNull(specificDataSet)), environment, userId);
    }
}
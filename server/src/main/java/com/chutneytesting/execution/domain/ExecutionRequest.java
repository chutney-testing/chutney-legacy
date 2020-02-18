package com.chutneytesting.execution.domain;

import com.chutneytesting.design.domain.scenario.TestCase;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;

    public ExecutionRequest(TestCase testCase, String environment) {
        this.testCase = testCase;
        this.environment = environment;
    }
}

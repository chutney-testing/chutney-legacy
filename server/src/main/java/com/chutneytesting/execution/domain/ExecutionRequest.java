package com.chutneytesting.execution.domain;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.design.domain.scenario.TestCase;
import java.util.Map;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;
    public final boolean withScenarioDefaultDataSet;

    public ExecutionRequest(TestCase testCase, String environment, boolean withScenarioDefaultDataSet) {
        this.testCase = testCase;
        this.environment = environment;
        this.withScenarioDefaultDataSet = withScenarioDefaultDataSet;
    }

    public ExecutionRequest(TestCase testCase, String environment) {
        this(testCase, environment, false);
    }

    public ExecutionRequest(TestCase testCase, String environment, Map<String, String> specificDataSet) {
        this(requireNonNull(testCase).withDataSet(requireNonNull(specificDataSet)), environment);
    }
}

package com.chutneytesting.server.core.domain.execution;

import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.scenario.TestCase;

public class ExecutionRequest {

    public final TestCase testCase;
    public final String environment;
    public final String userId;
    public final DataSet dataset;

    public ExecutionRequest(TestCase testCase, String environment, String userId, DataSet dataset) {
        this.testCase = testCase;
        this.environment = environment;
        this.userId = userId;
        this.dataset = dataset;
    }

}

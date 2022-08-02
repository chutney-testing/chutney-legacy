package com.chutneytesting.server.core.execution.processor;

import com.chutneytesting.server.core.execution.ExecutionRequest;
import com.chutneytesting.server.core.scenario.TestCase;
import java.util.Collections;
import java.util.List;

public class TestCasePreProcessors {

    private final List<TestCasePreProcessor> processors;

    public TestCasePreProcessors(List<TestCasePreProcessor> processors) {
        this.processors = Collections.unmodifiableList(processors);
    }

    public <T extends TestCase> T apply(ExecutionRequest executionRequest) {
        T tmp = (T) executionRequest.testCase;
        for (TestCasePreProcessor<T> p : processors) {
            if (p.test(tmp)) {
                tmp = p.apply(executionRequest);
            }
        }
        return tmp;
    }
}

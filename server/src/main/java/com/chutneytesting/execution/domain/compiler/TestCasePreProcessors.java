package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.TestCase;
import java.util.Collections;
import java.util.List;

public class TestCasePreProcessors {

    private final List<TestCasePreProcessor> processors;

    public TestCasePreProcessors(List<TestCasePreProcessor> processors) {
        processors.sort(new TestCasePreProcessor.PreProcessorComparator());
        this.processors = Collections.unmodifiableList(processors);
    }

    public <T extends TestCase> T apply(final T testCase) {
        T tmp = testCase;
        for (TestCasePreProcessor<T> p : processors) {
            if(p.test(tmp)) {
                tmp = p.apply(tmp);
            }
        }
        return tmp;
    }
}

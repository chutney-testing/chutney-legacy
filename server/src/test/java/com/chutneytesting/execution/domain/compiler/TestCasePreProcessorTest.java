package com.chutneytesting.execution.domain.compiler;

import com.google.common.collect.Lists;
import com.chutneytesting.design.domain.scenario.TestCase;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TestCasePreProcessorTest {

    @Test
    public void should_sort_preprocessors_in_descending_order() {
        TestCasePreProcessor.PreProcessorComparator sut = new TestCasePreProcessor.PreProcessorComparator();

        List<TestCasePreProcessor> processors = Lists.newArrayList(
            new TestCasePreProcessor() {
                @Override public TestCase apply(TestCase testCase) { return testCase; }
                @Override public int order() { return -1; }
            },

            new TestCasePreProcessor() {
                @Override public TestCase apply(TestCase testCase) { return testCase; }
                @Override public int order() { return 0; }
            },

            new TestCasePreProcessor() {
                @Override public TestCase apply(TestCase testCase) { return testCase; }
                @Override public int order() { return 10; }
            }
        );

        // when
        processors.sort(sut);

        // then
        Assertions.assertThat(processors.get(0).order()).isEqualTo(10);
        Assertions.assertThat(processors.get(1).order()).isEqualTo(0);
        Assertions.assertThat(processors.get(2).order()).isEqualTo(-1);
    }
}

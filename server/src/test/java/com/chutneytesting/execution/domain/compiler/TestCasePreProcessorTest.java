package com.chutneytesting.execution.domain.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class TestCasePreProcessorTest {

    @Test
    public void should_escape_new_line_and_carriage_return() {
        TestCasePreProcessor mock = Mockito.mock(TestCasePreProcessor.class);
        when(mock.replaceParams(Mockito.any(), Mockito.any())).thenCallRealMethod();
        Map<String, String> dataset = new HashMap<>();
        dataset.put("value","http://host:port/path");
        dataset.put("value_multiline","first line \n seconde line \r third line");

        String resultSingleLine = mock.replaceParams(dataset, "to be replaced: **value**");
        assertThat(resultSingleLine).isEqualTo("to be replaced: http://host:port/path");

        String resultMultiLine = mock.replaceParams(dataset, "to be replaced: **value_multiline**");
        assertThat(resultMultiLine).isEqualTo("to be replaced: first line \\n seconde line \\n third line");
    }

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
        assertThat(processors.get(0).order()).isEqualTo(10);
        assertThat(processors.get(1).order()).isEqualTo(0);
        assertThat(processors.get(2).order()).isEqualTo(-1);
    }
}

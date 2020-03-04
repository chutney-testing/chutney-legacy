package com.chutneytesting.execution.domain.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.scenario.TestCase;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

public class TestCasePreProcessorTest {

    @Test
    public void should_not_escape_by_default() {
        // Given
        TestCasePreProcessor mock = mock(TestCasePreProcessor.class);
        when(mock.replaceParams(any(String.class), any(Map.class), any(Map.class))).thenCallRealMethod();
        when(mock.replaceParams(any(String.class), any(Map.class), any(Map.class), any(Function.class))).thenCallRealMethod();
        when(mock.replaceParams(any(Map.class), any(String.class), any(Function.class))).thenCallRealMethod();

        String globalDatasetKeyToBeReplace = "globalKey";
        String datasetKeyToBeReplace = "key";

        Map<String, String> globalDataset = new HashMap<>();
        globalDataset.put(globalDatasetKeyToBeReplace, "http://host:port/path");
        Map<String, String> dataset = new HashMap<>();
        dataset.put(datasetKeyToBeReplace, "first line \n seconde line \r third line");

        // When
        String resultSingleLine = mock.replaceParams("to be replaced: **" + globalDatasetKeyToBeReplace+"**", globalDataset, dataset);
        assertThat(resultSingleLine).isEqualTo("to be replaced: " + globalDataset.get(globalDatasetKeyToBeReplace));

        String resultMultiLine = mock.replaceParams("to be replaced: **" + datasetKeyToBeReplace+"**", globalDataset, dataset);
        assertThat(resultMultiLine).isEqualTo("to be replaced: " + dataset.get(datasetKeyToBeReplace));
    }

    @Test
    public void should_escape_when_ask_for() {
        // Given
        TestCasePreProcessor mock = mock(TestCasePreProcessor.class);
        when(mock.replaceParams(any(String.class), any(Map.class), any(Map.class))).thenCallRealMethod();
        when(mock.replaceParams(any(String.class), any(Map.class), any(Map.class), any(Function.class))).thenCallRealMethod();
        when(mock.replaceParams(any(Map.class), any(String.class), any(Function.class))).thenCallRealMethod();

        String globalDatasetKeyToBeReplace = "globalKey";
        String datasetKeyToBeReplace = "key";

        Map<String, String> globalDataset = new HashMap<>();
        globalDataset.put(globalDatasetKeyToBeReplace, "http://host:port/path");
        Map<String, String> dataset = new HashMap<>();
        dataset.put(datasetKeyToBeReplace, "first line \n seconde line \r third line");

        // When
        String resultSingleLine = mock.replaceParams("to be replaced: **" + globalDatasetKeyToBeReplace+"**", globalDataset, dataset, input -> StringEscapeUtils.escapeJson((String) input));
        assertThat(resultSingleLine).isEqualTo("to be replaced: " + StringEscapeUtils.escapeJson(globalDataset.get(globalDatasetKeyToBeReplace)));

        String resultMultiLine = mock.replaceParams("to be replaced: **" + datasetKeyToBeReplace+"**", globalDataset, dataset, input -> StringEscapeUtils.escapeJson((String) input));
        assertThat(resultMultiLine).isEqualTo("to be replaced: "+ StringEscapeUtils.escapeJson(dataset.get(datasetKeyToBeReplace)));
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

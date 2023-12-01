/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.server.core.domain.execution.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

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

}

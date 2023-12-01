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

package com.chutneytesting.server.core.domain.dataset;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class DatasetTest {

    @Test
    void should_strip_whitespaces_in_name() {
        String expectedName = "name with spaces";
        DataSet actual = DataSet.builder().withName("   name   with   spaces  ").build();
        assertThat(actual.name).isEqualTo(expectedName);
    }

    @Test
    public void should_not_have_empty_id() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DataSet.builder().withId("").build())
            .withMessage("Dataset id cannot be empty");
    }

    @Test
    public void should_get_rid_of_empty_keys_and_lines() {
        // Edge case
        DataSet dataSet = DataSet.builder()
            .withName("my name")
            .withConstants(
                Map.of("", "")
            )
            .withDatatable(asList(
                Map.of("", ""),
                Map.of("", "value")
            ))
            .build();

        assertThat(dataSet.constants).isEmpty();
        assertThat(dataSet.datatable).isEmpty();

        // Normal case
        Map<String, String> expectedMap = Map.of("key1", "value", "key2", "value");
        dataSet = DataSet.builder()
            .withName("my name")
            .withConstants(
                Map.of("key1", "value", "", "value", "key2", "value")
            )
            .withDatatable(asList(
                Map.of("key1", "value", "", "", "key2", "value"),
                Map.of("key1", "", "", "", "key2", ""),
                Map.of("key1", "value", "", "value", "key2", "value")
            ))
            .build();

        assertThat(dataSet.constants).containsExactlyInAnyOrderEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactly(expectedMap, expectedMap);
    }

    @Test
    public void should_strip_whitespaces_in_keys_and_values() {
        Map<String, String> expectedMap = Map.of("key1", "value", "key2", "value");
        DataSet dataSet = DataSet.builder()
            .withName("my name")
            .withConstants(
                Map.of("key1 ", "value ", "", "value", " key2   ", "value")
            )
            .withDatatable(asList(
                Map.of("key1", " value", "", "", "key2     ", "value"),
                Map.of("key1", "", "", "", "key2  ", ""),
                Map.of("key1 ", "value", "", " value", "key2", "value")
            ))
            .build();

        assertThat(dataSet.constants).containsExactlyInAnyOrderEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactly(expectedMap, expectedMap);
    }
}

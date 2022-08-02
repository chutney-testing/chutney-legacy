package com.chutneytesting.component.dataset.domain;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class DataSetTest {

    @Test
    public void should_not_have_empty_id() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DataSet.builder().withId("").build());
    }

    @Test
    public void should_get_rid_of_empty_keys_and_lines() {
        // Edge case
        DataSet dataSet = DataSet.builder()
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
            .withConstants(
                Map.of("key1", "value", "", "value", "key2", "value")
            )
            .withDatatable(asList(
                Map.of("key1", "value", "", "", "key2", "value"),
                Map.of("key1", "", "", "", "key2", ""),
                Map.of("key1", "value", "", "value", "key2", "value")
            ))
            .build();

        assertThat(dataSet.constants).containsExactlyEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactly(expectedMap, expectedMap);
    }

    @Test
    public void should_remove_space_in_extremity_of_keys_and_values() {
        Map<String, String> expectedMap = Map.of("key1", "value", "key2", "value");
        DataSet dataSet = DataSet.builder()
            .withConstants(
                Map.of("key1 ", "value ", "", "value", " key2   ", "value")
            )
            .withDatatable(asList(
                Map.of("key1", " value", "", "", "key2     ", "value"),
                Map.of("key1", "", "", "", "key2  ", ""),
                Map.of("key1 ", "value", "", " value", "key2", "value")
            ))
            .build();

        assertThat(dataSet.constants).containsExactlyEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactly(expectedMap, expectedMap);
    }
}

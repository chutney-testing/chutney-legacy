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

        assertThat(dataSet.constants).containsExactlyInAnyOrderEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactly(expectedMap, expectedMap);
    }

    @Test
    public void should_strip_whitespaces_in_keys() {
        Map<String, String> expectedMap = Map.of("key1", "value ", "key2", "value");
        DataSet dataSet = DataSet.builder()
            .withConstants(
                Map.of("key1 ", "value ", "", "value", " key2   ", "value")
            )
            .withDatatable(asList(
                Map.of(" key1 ", "", "", "", "key2  ", "  value  "),
                Map.of(" key1 ", "  value", "", "", "key2  ", "")
            ))
            .build();

        assertThat(dataSet.constants).containsExactlyInAnyOrderEntriesOf(expectedMap);
        assertThat(dataSet.datatable).containsExactlyInAnyOrder(
            Map.of("key1", "", "key2", "  value  "),
            Map.of("key1", "  value", "key2", "")
        );
    }
}

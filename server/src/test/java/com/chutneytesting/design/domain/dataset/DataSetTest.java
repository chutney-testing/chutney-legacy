package com.chutneytesting.design.domain.dataset;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.Test;

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
            .withUniqueValues(
                Maps.of("", "")
            )
            .withMultipleValues(asList(
                Maps.of("", ""),
                Maps.of("", "value")
            ))
            .build();

        assertThat(dataSet.uniqueValues).isEmpty();
        assertThat(dataSet.multipleValues).isEmpty();

        // Normal case
        Map<String, String> exepectedMap = Maps.of("key1", "value", "key2", "value");
        dataSet = DataSet.builder()
            .withUniqueValues(
                Maps.of("key1", "value", "", "value", "key2", "value")
            )
            .withMultipleValues(asList(
                Maps.of("key1", "value", "", "", "key2", "value"),
                Maps.of("key1", "", "", "", "key2", ""),
                Maps.of("key1", "value", "", "value", "key2", "value")
            ))
            .build();

        assertThat(dataSet.uniqueValues).containsExactlyEntriesOf(exepectedMap);
        assertThat(dataSet.multipleValues).containsExactly(exepectedMap, exepectedMap);
    }
}

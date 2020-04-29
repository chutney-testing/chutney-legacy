package com.chutneytesting.design.domain.dataset;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.Test;

public class DataSetTest {

    @Test
    public void should_have_a_name() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DataSet.builder().build());

        assertThatIllegalArgumentException()
            .isThrownBy(() -> buildDataSetWithName("").build());
    }

    @Test
    public void should_not_have_empty_id() {
        DataSet.DataSetBuilder b = buildDataSetWithName("name");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> b.withId("").build());
    }

    private DataSet.DataSetBuilder buildDataSetWithName(String name) {
        return DataSet.builder().withMetaData(DataSetMetaData.builder().withName(name).build());
    }
}

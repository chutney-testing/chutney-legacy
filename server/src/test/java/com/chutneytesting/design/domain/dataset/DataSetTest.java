package com.chutneytesting.design.domain.dataset;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.Test;

public class DataSetTest {

    @Test
    public void should_not_have_empty_id() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> DataSet.builder().withId("").build());
    }
}

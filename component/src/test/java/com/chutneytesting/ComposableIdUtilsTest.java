package com.chutneytesting;


import static com.chutneytesting.ComposableIdUtils.toExternalId;
import static com.chutneytesting.ComposableIdUtils.toInternalId;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComposableIdUtilsTest {

    private final String EXTERNAL_ID = "30-1";
    private final String INTERNAL_ID = "#30:1";

    @Test
    public void should_map_to_external_id() {
        assertThat(toExternalId(INTERNAL_ID)).isEqualTo(EXTERNAL_ID);
    }

    @Test
    public void should_map_to_internal_id() {
        assertThat(toInternalId(EXTERNAL_ID)).isEqualTo(INTERNAL_ID);
    }

    @Test
    public void should_map_empty_to_internal_id() {
        assertThat(toInternalId("")).isEqualTo("");
    }
}

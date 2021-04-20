package com.chutneytesting.admin.domain.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ChutneyContentTest {

    @Test
    void should_throw_when_content_name_is_missing() {
        // Given
        ChutneyContent.ChutneyContentBuilder sut = ChutneyContent.builder();

        // When
        assertThatThrownBy(sut::build)
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("name required");
    }

    @Test
    void should_throw_when_content_category_is_missing() {
        // Given
        ChutneyContent.ChutneyContentBuilder sut = ChutneyContent.builder().withName("fake");

        // When
        assertThatThrownBy(sut::build)
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("category required");
    }

    @Test
    void should_throw_when_content_provider_is_missing() {
        // Given
        ChutneyContent.ChutneyContentBuilder sut = ChutneyContent.builder()
            .withName("fake").withCategory(ChutneyContentCategory.CONF);

        // When
        assertThatThrownBy(sut::build)
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("provider required");
    }

    @Test
    void should_default_format_to_json() {
        // Given
        ChutneyContent.ChutneyContentBuilder sut = ChutneyContent.builder()
            .withName("fake").withCategory(ChutneyContentCategory.CONF).withProvider("provider");

        // When
        assertThat(sut.build().format).isEqualTo("json");
    }

    @Test
    void should_default_content_to_empty() {
        // Given
        ChutneyContent.ChutneyContentBuilder sut = ChutneyContent.builder()
            .withName("fake").withCategory(ChutneyContentCategory.CONF).withProvider("provider");

        // When
        assertThat(sut.build().content).isEmpty();
    }
}

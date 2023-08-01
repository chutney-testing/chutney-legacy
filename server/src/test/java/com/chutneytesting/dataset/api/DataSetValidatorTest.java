package com.chutneytesting.dataset.api;

import com.chutneytesting.server.core.domain.tools.ui.ImmutableKeyValue;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataSetValidatorTest {
    @Test
    void should_reject_null_dataset() {
        // Given
        DataSetDto dataSetDto = null;

        // When / Then
        assertThrows(
            NoSuchElementException.class,
            () -> DataSetValidator.validateDatasetSave(dataSetDto)
        );
    }

    @Test
    void should_accept_empty_dataset() {
        // Given
        DataSetDto dataSetDto = ImmutableDataSetDto
            .builder()
            .name("toto")
            .datatable(List.of())
            .build();

        // When / Then
        assertDoesNotThrow(() -> DataSetValidator.validateDatasetSave(dataSetDto));
    }

    @Test
    void should_accept_dataset_with_unique_headers() {
        // Given
        DataSetDto dataSetDto = ImmutableDataSetDto
            .builder()
            .name("toto")
            .datatable(List.of(
                List.of(
                    ImmutableKeyValue.builder().key("toto").value("tata").build(),
                    ImmutableKeyValue.builder().key("titi").value("tutu").build()
                )
            ))
            .build();

        // When / Then
        assertDoesNotThrow(() -> DataSetValidator.validateDatasetSave(dataSetDto));
    }

    @Test
    void should_reject_dataset_with_duplicated_headers() {
        // Given
        DataSetDto dataSetDto = ImmutableDataSetDto
            .builder()
            .name("toto")
            .datatable(List.of(
                List.of(
                    ImmutableKeyValue.builder().key("toto").value("tata").build(),
                    ImmutableKeyValue.builder().key("toto").value("tutu").build()
                )
            ))
            .build();

        // When / Then
        assertThrows(
            IllegalArgumentException.class,
            () -> DataSetValidator.validateDatasetSave(dataSetDto)
        );
    }
}

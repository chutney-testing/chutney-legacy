package com.chutneytesting.dataset.infra;

import com.chutneytesting.server.core.domain.dataset.DataSet;
import java.time.Instant;

public class DatasetMapper {

    public static DatasetDto toDto(DataSet dataSet) {
        return new DatasetDto(
            dataSet.name,
            dataSet.description,
            dataSet.tags,
            dataSet.constants,
            dataSet.datatable
        );
    }

    public static DataSet fromDto(DatasetDto dto, Instant creationDate) {
        return DataSet.builder()
            .withId(dto.id)
            .withName(dto.name)
            .withDescription(dto.description)
            .withCreationDate(creationDate)
            .withTags(dto.tags)
            .withConstants(dto.constants)
            .withDatatable(dto.datatable)
            .build();
    }
}

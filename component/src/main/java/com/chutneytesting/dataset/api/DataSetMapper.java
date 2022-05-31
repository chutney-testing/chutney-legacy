package com.chutneytesting.dataset.api;


import com.chutneytesting.tools.ui.KeyValue;
import com.chutneytesting.dataset.domain.DataSet;
import java.util.stream.Collectors;

public class DataSetMapper {

    public static DataSetDto toDto(DataSet dataSet, Integer version) {
        return ImmutableDataSetDto.builder()
            .id(dataSet.id)
            .name(dataSet.name)
            .version(version)
            .description(dataSet.description)
            .lastUpdated(dataSet.creationDate)
            .tags(dataSet.tags)
            .constants(KeyValue.fromMap(dataSet.constants))
            .datatable(dataSet.datatable.stream().map(KeyValue::fromMap).collect(Collectors.toList()))
            .build();
    }

    public static DataSet fromDto(DataSetDto dto) {
        return DataSet.builder()
            .withId(dto.id().orElse(null))
            .withName(dto.name())
            .withDescription(dto.description())
            .withCreationDate(dto.lastUpdated())
            .withTags(dto.tags())
            .withConstants(KeyValue.toMap(dto.constants()))
            .withDatatable(dto.datatable().stream().map(KeyValue::toMap).collect(Collectors.toList()))
            .build();
    }
}

package com.chutneytesting.design.api.dataset;

import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;

import com.chutneytesting.tools.ui.KeyValue;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.tools.ui.ComposableIdUtils;
import java.util.stream.Collectors;

public class DataSetMapper {

    public static DataSetDto toDto(DataSet dataSet, Integer version) {
        return ImmutableDataSetDto.builder()
            .id(toFrontId(dataSet.id))
            .name(dataSet.name)
            .version(version)
            .description(dataSet.description)
            .lastUpdated(dataSet.creationDate)
            .tags(dataSet.tags)
            .uniqueValues(KeyValue.fromMap(dataSet.constants))
            .multipleValues(dataSet.datatable.stream().map(KeyValue::fromMap).collect(Collectors.toList()))
            .build();
    }

    public static DataSet fromDto(DataSetDto dto) {
        return DataSet.builder()
            .withId(dto.id().map(ComposableIdUtils::fromFrontId).orElse(null))
            .withName(dto.name())
            .withDescription(dto.description())
            .withCreationDate(dto.lastUpdated())
            .withTags(dto.tags())
            .withConstants(KeyValue.toMap(dto.uniqueValues()))
            .withDatatable(dto.multipleValues().stream().map(KeyValue::toMap).collect(Collectors.toList()))
            .build();
    }
}

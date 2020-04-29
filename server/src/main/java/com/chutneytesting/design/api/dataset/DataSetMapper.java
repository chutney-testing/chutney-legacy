package com.chutneytesting.design.api.dataset;

import static com.chutneytesting.tools.ui.OrientUtils.toFrontId;

import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetMetaData;
import com.chutneytesting.tools.ui.OrientUtils;
import java.util.stream.Collectors;

public class DataSetMapper {

    public static DataSetDto toDto(String dataSetId, DataSetMetaData dataSetMetaData) {
        return ImmutableDataSetDto.builder()
            .id(toFrontId(dataSetId))
            .name(dataSetMetaData.name)
            .description(dataSetMetaData.description)
            .lastUpdated(dataSetMetaData.creationDate)
            .tags(dataSetMetaData.tags)
            .build();
    }

    public static DataSetDto toDto(DataSet dataSet) {
        return ImmutableDataSetDto.builder()
            .id(toFrontId(dataSet.id))
            .name(dataSet.metadata.name)
            .description(dataSet.metadata.description)
            .lastUpdated(dataSet.metadata.creationDate)
            .tags(dataSet.metadata.tags)
            .uniqueValues(KeyValue.fromMap(dataSet.uniqueValues))
            .multipleValues(dataSet.multipleValues.stream().map(KeyValue::fromMap).collect(Collectors.toList()))
            .build();
    }

    public static DataSet fromDto(DataSetDto dto) {
        return DataSet.builder()
            .withId(dto.id().map(OrientUtils::fromFrontId).orElse(null))
            .withMetaData(
                DataSetMetaData.builder()
                    .withName(dto.name())
                    .withDescription(dto.description())
                    .withCreationDate(dto.lastUpdated())
                    .withTags(dto.tags())
                    .build())
            .withUniqueValues(KeyValue.toMap(dto.uniqueValues()))
            .withMultipleValues(dto.multipleValues().stream().map(KeyValue::toMap).collect(Collectors.toList()))
            .build();
    }
}

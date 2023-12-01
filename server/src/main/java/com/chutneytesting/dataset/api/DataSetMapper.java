/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.dataset.api;


import com.chutneytesting.server.core.domain.dataset.DataSet;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import java.util.stream.Collectors;

public class DataSetMapper {

    public static DataSetDto toDto(DataSet dataSet) {
        return ImmutableDataSetDto.builder()
            .id(dataSet.id)
            .name(dataSet.name)
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

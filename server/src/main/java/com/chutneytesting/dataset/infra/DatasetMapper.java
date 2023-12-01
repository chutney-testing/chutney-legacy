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

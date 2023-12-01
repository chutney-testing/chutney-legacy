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

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataSetDto.class)
@JsonDeserialize(as = ImmutableDataSetDto.class)
@Value.Style(jdkOnly = true)
public interface DataSetDto {

    Optional<String> id();
    String name();

    @Value.Default()
    default Integer version() {
        return 0;
    }

    @Value.Default()
    default String description() {
        return "";
    }

    @Value.Default()
    default Instant lastUpdated() {
        return now();
    }

    @Value.Default()
    default List<String> tags() {
        return emptyList();
    }

    @Value.Default()
    @JsonProperty("uniqueValues")
    default List<KeyValue> constants() {
        return emptyList();
    }

    @Value.Default()
    @JsonProperty("multipleValues")
    default List<List<KeyValue>> datatable() {
        return emptyList();
    }

    default List<String> duplicatedHeaders() {
        if(!datatable().isEmpty()) {
            List<String> headers = datatable().get(0).stream().map(KeyValue::key).toList();
            return headers.stream()
                .collect(groupingBy(h -> h, counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        }
        return emptyList();
    }
}

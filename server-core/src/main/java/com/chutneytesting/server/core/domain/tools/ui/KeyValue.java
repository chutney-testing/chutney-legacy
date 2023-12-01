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

package com.chutneytesting.server.core.domain.tools.ui;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableKeyValue.class)
@JsonDeserialize(as = ImmutableKeyValue.class)
@Value.Style(jdkOnly = true)
public interface KeyValue {

    String key();

    @Value.Default
    default String value() {
        return "";
    }

    static List<KeyValue> fromMap(Map<String, String> map) {
        return map.keySet().stream()
            .map((key) -> ImmutableKeyValue.builder()
                .key(key)
                .value(map.get(key))
                .build())
            .collect(Collectors.toList());
    }

    static Map<String, String> toMap(List<KeyValue> list) {
        return ofNullable(list)
            .map(l -> l.stream()
                .filter(kv -> StringUtils.isNoneBlank(kv.key()))
                .collect(Collectors.toMap(KeyValue::key, KeyValue::value, (k1, k2) -> k1, LinkedHashMap::new)))
            .orElseGet(LinkedHashMap::new);
    }
}

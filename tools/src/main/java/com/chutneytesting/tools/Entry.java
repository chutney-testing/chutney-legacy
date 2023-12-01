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

package com.chutneytesting.tools;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Entry {
    public final String key;
    public final String value;

    public Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static List<Entry> toEntryList(Map<String, String> properties) {
        return properties.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(toList());
    }

    public static Set<Entry> toEntrySet(Map<String, String> properties) {
        return properties.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(toSet());
    }

    public static Map<String, String> toMap(Collection<Entry> entries) {
        return entries.stream()
            .collect(Collectors.toMap(e -> e.key, e -> e.value, (k1, k2) -> k1, LinkedHashMap::new));
    }
}

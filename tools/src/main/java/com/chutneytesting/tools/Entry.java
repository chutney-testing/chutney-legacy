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

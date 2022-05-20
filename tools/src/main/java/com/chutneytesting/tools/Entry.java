package com.chutneytesting.tools;

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
            .collect(Collectors.toList());
    }

    public static Set<Entry> toEntrySet(Map<String, String> properties) {
        return properties.entrySet().stream()
            .map(e -> new Entry(e.getKey(), e.getValue()))
            .collect(Collectors.toSet());
    }

}

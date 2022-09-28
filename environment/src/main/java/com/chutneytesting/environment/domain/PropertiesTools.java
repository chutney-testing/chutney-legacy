package com.chutneytesting.environment.domain;

import com.chutneytesting.tools.Entry;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertiesTools {
    public final static String HIDDEN_PASSWORD = "**********";

    public static Set<Entry> hidePassword(Set<Entry> set) {
        return set.stream()
            .map(e -> new Entry(e.key, (isPasswordKeyword(e.key)) ? HIDDEN_PASSWORD : e.value))
            .collect(Collectors.toSet());
    }

    static boolean propertiesEquals(Map<String, String> a, Map<String, String> b) {
        return a.keySet().equals(b.keySet())
            && b.entrySet().stream()
            .filter(e -> !isPasswordEntry(e) || !e.getValue().equals(HIDDEN_PASSWORD))
            .allMatch(e -> a.containsKey(e.getKey())
                && a.get(e.getKey()).equals(e.getValue()));
    }

    static boolean isPasswordKeyword(String keyword) {
        Set<String> passwordKeywordList = Set.of("password", "pwd");
        return passwordKeywordList.stream().anyMatch(s -> keyword.toLowerCase(Locale.ROOT).contains(s));
    }

    static boolean isPasswordEntry(Map.Entry<String, String> entry) {
        return isPasswordKeyword(entry.getKey());
    }
}

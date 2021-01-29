package com.chutneytesting.tools.ui;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashMap;
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

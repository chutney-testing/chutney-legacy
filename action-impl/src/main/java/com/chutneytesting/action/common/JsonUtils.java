package com.chutneytesting.action.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

public final class JsonUtils {

    private static final ObjectMapper om = new ObjectMapper();

    /**
     * Serialize given object as JSON string.
     *
     * @param obj The object to serialize.
     * @return JSON string representation.
     */
    public static String jsonStringify(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String string) {
            return string;
        } else {
            try {
                return om.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Can't convert obj as json string: " + obj, e);
            }
        }
    }

    /**
     * Json lenient comparison
     *
     * @param read1        Result of a json document read
     * @param read2        Result of another json document read
     * @param leftContains is read1 lenient contains read2 ?
     * @return The result of the comparison as boolean
     */
    @SuppressWarnings("unchecked")
    public static boolean lenientEqual(Object read1, Object read2, Boolean leftContains) {
        if (read1 instanceof Map && read2 instanceof Map) {
            Map<Object, Object> map1 = (Map<Object, Object>) read1;
            Map<Object, Object> map2 = (Map<Object, Object>) read2;
            MapDifference<Object, Object> diff = Maps.difference(map1, map2);
            if (diff.areEqual()) {
                return true;
            } else if ((leftContains == null || !leftContains) && diff.entriesOnlyOnLeft().isEmpty()) {
                return diff.entriesDiffering().keySet().stream()
                    .map(k -> lenientEqual(map1.get(k), map2.get(k), false))
                    .reduce(Boolean::logicalAnd).orElse(true);
            } else if ((leftContains == null || leftContains) && diff.entriesOnlyOnRight().isEmpty()) {
                return diff.entriesDiffering().keySet().stream()
                    .map(k -> lenientEqual(map1.get(k), map2.get(k), true))
                    .reduce(Boolean::logicalAnd).orElse(true);
            }
            return false;
        } else if (read1 instanceof List && read2 instanceof List) {
            List<Object> list1 = (List<Object>) read1;
            List<Object> list2 = (List<Object>) read2;
            return list1.size() == list2.size() && list1.containsAll(list2);
        } else {
            return read1.equals(read2);
        }
    }
}

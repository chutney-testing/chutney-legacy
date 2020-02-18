package com.chutneytesting.task.amqp.utils;

import com.rabbitmq.client.LongString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AmqpUtils {

    /**
     * Converts all values of the given Map and type LongString to String.
     * In case of map is null, null will be directly returned.
     *
     * @return consolidated map.
     */
    public static Map<String, Object> convertMapLongStringToString(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return map.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> convertLongStringToString(e.getValue())));

    }

    /**
     * Converts the given object in case of a LongString or List including a LongString to String.
     *
     * @return consolidated object
     */
    private static Object convertLongStringToString(Object value) {

        if (value instanceof LongString) {
            return value.toString();
        }

        if (value instanceof List) {
            List<Object> newList = new ArrayList<>();
            for (Object item : (List<?>) value) {
                newList.add(convertLongStringToString(item));
            }
            return newList;
        }

        if (value instanceof Map) {
            return convertMapLongStringToString((Map) value);
        }

        return value;
    }
}

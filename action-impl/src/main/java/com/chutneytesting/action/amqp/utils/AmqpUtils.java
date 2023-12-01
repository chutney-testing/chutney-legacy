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

package com.chutneytesting.action.amqp.utils;

import com.rabbitmq.client.LongString;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            return Collections.emptyMap();
        }

        return map.entrySet().stream()
            .map(e-> new AbstractMap.SimpleEntry<>(
                e.getKey(),
                Optional.ofNullable(convertLongStringToString(e.getValue())).orElse("null")))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

        if (value instanceof List listValue) {
            var newList = new ArrayList<>();
            for (Object item : listValue) {
                newList.add(convertLongStringToString(item));
            }
            return newList;
        }

        if (value instanceof Map mapValue) {
            return convertMapLongStringToString(mapValue);
        }

        return value;
    }
}

package com.chutneytesting.task.assertion.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    /**
     * Serialize given object as JSON string.
     *
     * @param obj The object to serialize.
     * @return JSON string representation.
     */
    public static String jsonStringify(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Can't convert obj as json string: " + obj, e);
            }
        }
    }
}

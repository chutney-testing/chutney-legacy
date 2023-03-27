package com.chutneytesting.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public final class Jsons {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private Jsons() {
    }

    public static <T> T loadJsonFromClasspath(String path, Class<T> targetClass) {
        try {
            return OBJECT_MAPPER.readValue(Jsons.class.getClassLoader().getResourceAsStream(path), targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot deserialize " + path + " to " + targetClass.getSimpleName(), e);
        }
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}

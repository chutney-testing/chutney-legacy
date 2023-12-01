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

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

package com.chutneytesting.action.spi.injectable;

import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

public interface Target {

    String name();

    URI uri();

    String rawUri();

    Optional<String> property(String key);

    default Map<String, String> prefixedProperties(String prefix) {
        return prefixedProperties(prefix, false);
    }

    Map<String, String> prefixedProperties(String prefix, boolean cutPrefix);

    default Optional<Number> numericProperty(String key) {
        return property(key).map(k -> {
            try {
                return NumberFormat.getInstance().parse(k);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default Optional<Boolean> booleanProperty(String key) {
        return property(key).map(Boolean::parseBoolean);
    }

    default Optional<String> user() {
        return property("username")
            .or(() -> property("user"));
    }

    default Optional<String> userPassword() {
        return property("userPassword")
            .or(() -> property("password"));
    }

    default Optional<String> trustStore() {
        return property("trustStore");
    }

    default Optional<String> trustStorePassword() {
        return property("trustStorePassword");
    }

    default Optional<String> keyStore() {
        return property("keyStore");
    }

    default Optional<String> keyStorePassword() {
        return property("keyStorePassword");
    }

    default Optional<String> keyPassword() {
        return property("keyPassword");
    }

    default Optional<String> privateKey() {
        return property("privateKey");
    }

    default Optional<String> privateKeyPassword() {
        return property("privateKeyPassword")
            .or(() -> property("privateKeyPassphrase"));
    }

    default String host() {
        return uri().getHost();
    }

    default int port() {
        return uri().getPort();
    }
}

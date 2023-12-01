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

package com.chutneytesting.action;

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestActionsConfiguration implements ActionsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestActionsConfiguration.class);

    public final Map<String, String> configuration = new HashMap<>();

    @Override
    public String getString(String key) {
        return configuration.get(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return ofNullable(getString(key)).orElse(defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        String value = configuration.get(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException exception) {
                LOGGER.error("Cannot parse [{}] to Integer", value);
            }
        }
        return null;
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return ofNullable(getInteger(key)).orElse(defaultValue);
    }
}

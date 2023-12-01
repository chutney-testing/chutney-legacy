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

package com.chutneytesting.engine.domain.execution.strategies;

import java.util.HashMap;
import java.util.Map;

/**
 * Strategy parameters.
 */
@SuppressWarnings("serial")
public class StrategyProperties extends HashMap<String, Object> {

    public StrategyProperties() {
        super();
    }

    public StrategyProperties(Map<String, Object> data) {
        super(data);
    }

    public <T> T getProperty(String key, Class<T> type) {
        return type.cast(get(key));
    }

    public StrategyProperties setProperty(String key, Object value) {
        put(key, value);
        return this;
    }
}

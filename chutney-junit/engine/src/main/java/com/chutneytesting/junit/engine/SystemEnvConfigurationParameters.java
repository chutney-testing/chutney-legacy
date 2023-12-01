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

package com.chutneytesting.junit.engine;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.platform.engine.ConfigurationParameters;

public class SystemEnvConfigurationParameters implements ConfigurationParameters {

    private final ConfigurationParameters delegate;
    private final Map<String, String> env = System.getenv();

    public SystemEnvConfigurationParameters(ConfigurationParameters delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<String> get(String key) {
        Optional<String> delegateValue = delegate.get(key);

        if (delegateValue.isEmpty()) {
            return ofNullable(env.get(key));
        } else {
            return delegateValue;
        }
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        Optional<Boolean> delegateValue = delegate.getBoolean(key);

        if (delegateValue.isEmpty()) {
            return ofNullable(env.get(key)).map(Boolean::valueOf);
        } else {
            return delegateValue;
        }
    }

    @Override
    public int size() {
        return delegate.size() + env.size();
    }

    @Override
    public Set<String> keySet() {
        return env.keySet();
    }
}

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

package com.chutneytesting.engine.domain.execution.engine.scenario;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ScenarioContext extends Map<String, Object> {
    default ScenarioContext unmodifiable() {
        return new UnmodifiableScenarioContext(this);
    }

    <T> T getOrDefault(String key, T defaultValue);

    class UnmodifiableScenarioContext implements ScenarioContext {
        private final ScenarioContext scenarioContext;

        private UnmodifiableScenarioContext(ScenarioContext scenarioContext) {
            this.scenarioContext = scenarioContext;
        }

        public int size() {return scenarioContext.size();}
        public boolean isEmpty() {return scenarioContext.isEmpty();}
        public boolean containsKey(Object key) {return scenarioContext.containsKey(key);}
        public boolean containsValue(Object value) {return scenarioContext.containsValue(value);}
        public Object get(Object key) {return scenarioContext.get(key);}

        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        public Object remove(Object value) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        private transient Set<String> keySet;
        private transient Set<Entry<String,Object>> entrySet;
        private transient Collection<Object> values;

        public Set<String> keySet() {
            if (keySet==null)
                keySet = Collections.unmodifiableSet(scenarioContext.keySet());
            return keySet;
        }

        public Collection<Object> values() {
            if (values==null)
                values = Collections.unmodifiableCollection(scenarioContext.values());
            return values;
        }

        public Set<Entry<String, Object>> entrySet() {
            if (entrySet==null)
                entrySet = Collections.unmodifiableSet(scenarioContext.entrySet());
            return entrySet;
        }

        public boolean equals(Object o) {return o == this || scenarioContext.equals(o);}
        public int hashCode()           {return scenarioContext.hashCode();}
        public String toString()        {return scenarioContext.toString();}

        @Override
        public <T> T getOrDefault(String key, T defaultValue) {
            return scenarioContext.getOrDefault(key, defaultValue);
        }

        @Override
        public Object getOrDefault(Object key, Object defaultValue) {
            return scenarioContext.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {
            scenarioContext.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object putIfAbsent(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(String key, Object oldValue, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object replace(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException();
        }
    }
}

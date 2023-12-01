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

package com.chutneytesting.scenario.domain.gwt;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Strategy {

    public static final Optional<Strategy> NONE = empty();
    public static final Strategy DEFAULT = new Strategy("", emptyMap());

    public final String type;
    public final Map<String, Object> parameters;

    public Strategy(String type, Map<String, Object> parameters) {
        this.type = ofNullable(type).orElse("");
        this.parameters = ofNullable(parameters).orElse(emptyMap());
    }

    @Override
    public String toString() {
        return "Strategy{" +
            "type='" + type + '\'' +
            ", parameters=" + parameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strategy strategy = (Strategy) o;
        return type.equals(strategy.type) &&
            parameters.equals(strategy.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parameters);
    }

}

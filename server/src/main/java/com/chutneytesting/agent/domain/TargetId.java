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

package com.chutneytesting.agent.domain;

import java.util.Objects;

public final class TargetId {
    public final String name;
    public final String environment;

    public TargetId(String name, String environment) {
        this.name = Objects.requireNonNull(name, "name");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public static TargetId of(String name, String env) {
        return new TargetId(name, env);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetId targetId = (TargetId) o;
        return name.equals(targetId.name) &&
            environment.equals(targetId.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, environment);
    }
}

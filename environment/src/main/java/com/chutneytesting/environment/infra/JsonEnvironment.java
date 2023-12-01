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

package com.chutneytesting.environment.infra;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.environment.domain.Environment;
import java.util.Set;

public class JsonEnvironment {

    public String name;
    public String description;
    public Set<JsonTarget> targets;

    public JsonEnvironment() {
    }

    private JsonEnvironment(String name, String description, Set<JsonTarget> targets) {
        this.name = name;
        this.description = description;
        this.targets = targets;
    }

    public static JsonEnvironment from(Environment environment) {
        Set<JsonTarget> targets = environment.targets.stream().map(JsonTarget::from).collect(toSet());
        return new JsonEnvironment(environment.name, environment.description, targets);
    }

    public Environment toEnvironment() {

        return Environment.builder()
            .withName(name)
            .withDescription(description)
            .withTargets(ofNullable(targets).orElse(emptySet()).stream().map(t -> t.toTarget(name)).collect(toSet()))
            .build();
    }
}

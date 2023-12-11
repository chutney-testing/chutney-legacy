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

package com.chutneytesting.environment.api.target.dto;

import static com.chutneytesting.tools.Entry.toEntrySet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import com.chutneytesting.environment.domain.Target;
import com.chutneytesting.tools.Entry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TargetDto {
    public final String name;
    public final String url;
    public final String environment;
    public final Set<Entry> properties;

    public TargetDto() {
        this(null,null,null,null);
    }
    public TargetDto(String name,
                     String url,
                     Set<Entry> properties) {
        this(name, url, null, properties);
    }
    public TargetDto(String name,
                     String url,
                     String environment,
                     Set<Entry> properties) {
        this.name = trimIfNotNull(name);
        this.url = trimIfNotNull(url);
        this.environment = trimIfNotNull(environment);
        this.properties = nullToEmpty(properties);
    }

    public Target toTarget(String environment) {
        return Target.builder()
            .withName(name)
            .withEnvironment(environment)
            .withUrl(url)
            .withProperties(propertiesToMap())
            .build();
    }

    public Target toTarget() {
        return Target.builder()
            .withName(name)
            .withEnvironment(environment)
            .withUrl(url)
            .withProperties(propertiesToMap())
            .build();
    }

    public static TargetDto from(Target target) {
        return new TargetDto(
            target.name,
            target.url,
            target.environment,
            toEntrySet(target.properties)
        );
    }

    public Map<String, String> propertiesToMap() {
        return properties == null ? emptyMap() : properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value));
    }

    private <T> Set<T> nullToEmpty(Set<T> set) {
        return set == null ? emptySet() : set;
    }

    private String trimIfNotNull(String environment) {
        return environment != null ? environment.trim() : null;
    }
}

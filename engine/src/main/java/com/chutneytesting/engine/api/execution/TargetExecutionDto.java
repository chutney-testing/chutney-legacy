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

package com.chutneytesting.engine.api.execution;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TargetExecutionDto {

    public final String id;
    public final String url;
    public final Map<String, String> properties;
    public final String name;
    public final List<NamedHostAndPort> agents;

    public TargetExecutionDto(String id, String url, Map<String, String> properties, List<NamedHostAndPort> agents) {
        this.id = id;
        this.name = id;
        this.url = url;
        this.properties = properties;
        this.agents = agents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetExecutionDto targetDto = (TargetExecutionDto) o;
        return id.equals(targetDto.id) &&
            url.equals(targetDto.url) &&
            properties.equals(targetDto.properties) &&
            agents.equals(targetDto.agents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, properties, agents);
    }

    @Override
    public String toString() {
        return "TargetDto{" +
            "id='" + id + '\'' +
            ", url='" + url + '\'' +
            ", properties=" + properties +
            ", agents=" + agents +
            '}';
    }
}

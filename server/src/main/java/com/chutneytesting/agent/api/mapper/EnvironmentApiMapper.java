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

package com.chutneytesting.agent.api.mapper;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.tools.Entry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentApiMapper {

    public EnvironmentDto fromDto(EnvironmentApiDto environmentApiDto) {
        List<TargetDto> targets = environmentApiDto.targetsConfiguration.stream().map(this::fromDto).collect(toList());
        return new EnvironmentDto(environmentApiDto.name, null, targets);
    }

    private TargetDto fromDto(TargetsApiDto targetsApiDto) {
        Map<String, String> properties = new LinkedHashMap<>(targetsApiDto.properties);
        return new TargetDto(targetsApiDto.name, targetsApiDto.url, Entry.toEntrySet(properties));
    }

    public EnvironmentApiDto toDto(EnvironmentDto environment) {
        return new EnvironmentApiDto(environment.name, environment.targets.stream().map(this::toDto).collect(toSet()));
    }

    private TargetsApiDto toDto(TargetDto target) {
        Map<String, String> properties = Entry.toMap(target.properties);
        return new TargetsApiDto(target.name, target.url, properties);
    }
}

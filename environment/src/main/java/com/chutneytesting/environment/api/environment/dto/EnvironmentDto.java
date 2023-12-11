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

package com.chutneytesting.environment.api.environment.dto;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.environment.api.target.dto.TargetDto;
import com.chutneytesting.environment.domain.Environment;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnvironmentDto {

    public final String name;
    public final String description;
    public final List<TargetDto> targets;

    public EnvironmentDto(String name) {
        this.name = name;
        this.description = null;
        this.targets = emptyList();
    }

    public EnvironmentDto(String name, String description, List<TargetDto> targets) {
        this.name = name;
        this.description = description;
        this.targets = ofNullable(targets).map(Collections::unmodifiableList).orElse(emptyList());
    }

    public static EnvironmentDto from(Environment environment) {
        List<TargetDto> targets = environment.targets.stream().map(TargetDto::from).collect(toList());
        return new EnvironmentDto(environment.name, environment.description, targets);
    }

    public Environment toEnvironment() {
        return Environment.builder()
            .withName(name)
            .withDescription(description)
            .withTargets(
                ofNullable(targets).orElse(emptyList()).stream().map(t -> t.toTarget(name)).collect(Collectors.toSet())
            )
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnvironmentDto that = (EnvironmentDto) o;
        return Objects.equals(name, that.name) && Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, targets);
    }
}

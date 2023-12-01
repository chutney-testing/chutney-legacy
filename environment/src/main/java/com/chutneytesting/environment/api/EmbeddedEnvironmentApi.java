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

package com.chutneytesting.environment.api;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.domain.TargetFilter;
import com.chutneytesting.environment.domain.exception.AlreadyExistingEnvironmentException;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.CannotDeleteEnvironmentException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.InvalidEnvironmentNameException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EmbeddedEnvironmentApi implements EnvironmentApi {

    private final EnvironmentService environmentService;

    public EmbeddedEnvironmentApi(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Override
    public Set<EnvironmentDto> listEnvironments() {
        return environmentService.listEnvironments().stream()
            .map(EnvironmentDto::from)
            .sorted(Comparator.comparing(e -> e.name))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<String> listEnvironmentsNames() {
        return environmentService.listEnvironmentsNames();
    }

    @Override
    public EnvironmentDto getEnvironment(String environmentName) throws EnvironmentNotFoundException {
        return EnvironmentDto.from(environmentService.getEnvironment(environmentName));
    }

    @Override
    public EnvironmentDto createEnvironment(EnvironmentDto environmentMetadataDto, boolean force) throws InvalidEnvironmentNameException, AlreadyExistingEnvironmentException {
        return EnvironmentDto.from(environmentService.createEnvironment(environmentMetadataDto.toEnvironment(), force));
    }

    @Override
    public EnvironmentDto importEnvironment(EnvironmentDto environmentDto) throws UnsupportedOperationException {
        environmentService.createEnvironment(environmentDto.toEnvironment());
        return environmentDto;
    }

    @Override
    public void deleteEnvironment(String environmentName) throws EnvironmentNotFoundException, CannotDeleteEnvironmentException {
        environmentService.deleteEnvironment(environmentName);
    }

    @Override
    public void updateEnvironment(String environmentName, EnvironmentDto environmentMetadataDto) throws InvalidEnvironmentNameException, EnvironmentNotFoundException {
        environmentService.updateEnvironment(environmentName, environmentMetadataDto.toEnvironment());
    }

    @Override
    public List<TargetDto> listTargets(TargetFilter filters) throws EnvironmentNotFoundException {
        return environmentService.listTargets(filters).stream()
            .map(TargetDto::from)
            .sorted(Comparator.comparing(t -> t.name))
            .collect(toList());
    }

    @Override
    public Set<String> listTargetsNames() throws EnvironmentNotFoundException {
        return environmentService.listTargetsNames();
    }

    @Override
    public TargetDto getTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        return TargetDto.from(environmentService.getTarget(environmentName, targetName));
    }

    @Override
    public void addTarget(TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException {
        environmentService.addTarget(targetMetadataDto.toTarget());
    }

    @Override
    public TargetDto importTarget(String environmentName, TargetDto targetDto) {
        environmentService.addTarget(targetDto.toTarget(environmentName));
        return targetDto;
    }

    @Override
    public void updateTarget(String targetName, TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.updateTarget(targetName, targetMetadataDto.toTarget());
    }

    @Override
    public void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(environmentName, targetName);
    }

    @Override
    public void deleteTarget(String targetName) throws EnvironmentNotFoundException, TargetNotFoundException {
        environmentService.deleteTarget(targetName);
    }


}

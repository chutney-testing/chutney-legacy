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

package com.chutneytesting.environment.api.target;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.environment.api.target.dto.TargetDto;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.domain.TargetFilter;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class EmbeddedTargetApi implements  TargetApi {

    private final EnvironmentService environmentService;

    public EmbeddedTargetApi(EnvironmentService environmentService) {
        this.environmentService = environmentService;
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

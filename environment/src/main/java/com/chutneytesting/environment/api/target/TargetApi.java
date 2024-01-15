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

import com.chutneytesting.environment.api.target.dto.TargetDto;
import com.chutneytesting.environment.domain.TargetFilter;
import com.chutneytesting.environment.domain.exception.AlreadyExistingTargetException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import com.chutneytesting.environment.domain.exception.TargetNotFoundException;
import java.util.List;
import java.util.Set;

public interface TargetApi {
    List<TargetDto> listTargets(TargetFilter filter) throws EnvironmentNotFoundException;

    Set<String> listTargetsNames() throws EnvironmentNotFoundException;

    TargetDto getTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException;

    void addTarget(TargetDto targetMetadataDto) throws EnvironmentNotFoundException, AlreadyExistingTargetException;

    TargetDto importTarget(String environmentName, TargetDto targetDto);

    void updateTarget(String targetName, TargetDto targetMetadataDto) throws EnvironmentNotFoundException, TargetNotFoundException;

    void deleteTarget(String environmentName, String targetName) throws EnvironmentNotFoundException, TargetNotFoundException;

    void deleteTarget(String targetName) throws EnvironmentNotFoundException, TargetNotFoundException;

}

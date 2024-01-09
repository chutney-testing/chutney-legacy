/*
 *
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
 *
 */

package com.chutneytesting.environment.api.variable;


import com.chutneytesting.environment.api.variable.dto.EnvironmentVariableDto;
import com.chutneytesting.environment.domain.exception.VariableAlreadyExistingException;
import com.chutneytesting.environment.domain.exception.EnvVariableNotFoundException;
import com.chutneytesting.environment.domain.exception.EnvironmentNotFoundException;
import java.util.List;

public interface EnvironmentVariableApi {

    void addVariable(List<EnvironmentVariableDto> values) throws EnvironmentNotFoundException, VariableAlreadyExistingException;

    void updateVariable(String key, List<EnvironmentVariableDto> values) throws EnvironmentNotFoundException, EnvVariableNotFoundException;

    void deleteVariable(String key) throws  EnvVariableNotFoundException;

}

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

package com.chutneytesting.junit.engine;

import com.chutneytesting.environment.api.environment.EnvironmentApi;
import com.chutneytesting.environment.api.target.TargetApi;
import com.chutneytesting.environment.api.environment.dto.EnvironmentDto;
import com.chutneytesting.environment.api.target.dto.TargetDto;
import com.chutneytesting.junit.api.EnvironmentService;

public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentApi environmentApi;
    private final TargetApi targetApi;

    public EnvironmentServiceImpl(EnvironmentApi delegate, TargetApi targetApi) {
        this.environmentApi = delegate;
        this.targetApi = targetApi;
    }

    @Override
    public void addEnvironment(EnvironmentDto environment) {
        environmentApi.createEnvironment(environment);
    }

    @Override
    public void deleteEnvironment(String environmentName) {
        environmentApi.deleteEnvironment(environmentName);
    }

    @Override
    public void addTarget(TargetDto target) {
        targetApi.addTarget(target);
    }
}

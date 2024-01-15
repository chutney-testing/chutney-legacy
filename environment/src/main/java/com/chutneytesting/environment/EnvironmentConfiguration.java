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

package com.chutneytesting.environment;

import com.chutneytesting.environment.api.environment.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.target.EmbeddedTargetApi;
import com.chutneytesting.environment.api.variable.EmbeddedVariableApi;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;

public class EnvironmentConfiguration {

    public static final String DEFAULT_ENV_NAME = "DEFAULT";
    private final EnvironmentRepository environmentRepository;
    private final EmbeddedEnvironmentApi environmentApi;
    private final EmbeddedTargetApi targetApi;
    private final EmbeddedVariableApi variableApi;

    public EnvironmentConfiguration(String storeFolderPath) {
        this.environmentRepository = createEnvironmentRepository(storeFolderPath);
        EnvironmentService environmentService = createEnvironmentService(environmentRepository);
        this.environmentApi = new EmbeddedEnvironmentApi(environmentService);
        this.targetApi = new EmbeddedTargetApi(environmentService);
        this.variableApi = new EmbeddedVariableApi(environmentService);

        createDefaultEnvironment(environmentService);
    }

    private void createDefaultEnvironment(EnvironmentService environmentService) {
        if (environmentRepository.listNames().isEmpty()) {
            environmentService.createEnvironment(Environment.builder().withName(DEFAULT_ENV_NAME).build());
        }
    }

    private EnvironmentRepository createEnvironmentRepository(String storeFolderPath) {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }

    private EnvironmentService createEnvironmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    public EmbeddedEnvironmentApi getEmbeddedEnvironmentApi() {
        return environmentApi;
    }

    public EmbeddedTargetApi getEmbeddedTargetApi() {
        return targetApi;
    }

    public EmbeddedVariableApi getEmbeddedVariableApi() {
        return variableApi;
    }
}

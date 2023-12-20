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

import com.chutneytesting.engine.api.execution.EnvironmentDto;
import com.chutneytesting.environment.EnvironmentConfiguration;
import com.chutneytesting.environment.api.variable.dto.EnvironmentVariableDto;
import com.chutneytesting.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.junit.api.EnvironmentService;
import com.chutneytesting.tools.UncheckedException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChutneyTestEngine extends HierarchicalTestEngine<ChutneyEngineExecutionContext> {

    public static final String CHUTNEY_JUNIT_ENGINE_ID = "chutney-junit-engine";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChutneyTestEngine.class);
    public static final String CHUTNEY_JUNIT_ENV_PATH = ".chutney/junit/conf";

    @Override
    public String getId() {
        return CHUTNEY_JUNIT_ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("com.chutneytesting");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("chutney-junit-platform-engine");
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        try {
            ConfigurationParameters cp = new SystemEnvConfigurationParameters(engineDiscoveryRequest.getConfigurationParameters());
            GlacioAdapterConfiguration glacioAdapterConfiguration = new GlacioAdapterConfiguration(getEnvironmentDirectoryPath(cp));
            ChutneyEngineDescriptor engineDescriptor = new ChutneyEngineDescriptor(uniqueId, "Chutney", findChutneyClass(getEnvironmentDirectoryPath(cp)));
            new DiscoverySelectorResolver(glacioAdapterConfiguration.glacioAdapter(), getEnvironmentName(cp)).resolveSelectors(engineDiscoveryRequest, engineDescriptor);
            return engineDescriptor;
        } catch (Exception e) {
            LOGGER.error("{} discovery error", getId(), e);
            throw UncheckedException.throwUncheckedException(e);
        }
    }

    @Override
    protected ChutneyEngineExecutionContext createExecutionContext(ExecutionRequest executionRequest) {
        try {
            ConfigurationParameters cp = new SystemEnvConfigurationParameters(executionRequest.getConfigurationParameters());
            GlacioAdapterConfiguration glacioAdapterConfiguration = new GlacioAdapterConfiguration(getEnvironmentDirectoryPath(cp));
            return new ChutneyEngineExecutionContext(glacioAdapterConfiguration.executionConfiguration(), getEnvironment(cp));
        } catch (Exception e) {
            LOGGER.error("{} create execution context error", getId(), e);
            throw UncheckedException.throwUncheckedException(e);
        }
    }

    private Object findChutneyClass(String storeFolderPath) {
        return ReflectionSupport
            .findAllClassesInPackage("", aClass -> aClass.isAnnotationPresent(Chutney.class), x -> true)
            .stream()
            .findFirst()
            .map(aClass1 -> newInstance(aClass1, storeFolderPath))
            .orElse(null);
    }

    private Object newInstance(Class<?> aClass, String storeFolderPath) {
        try {
            try {
                Constructor<?> constructor = aClass.getConstructor(EnvironmentService.class);
                EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(storeFolderPath);
                return constructor.newInstance(new EnvironmentServiceImpl(environmentConfiguration.getEmbeddedEnvironmentApi(), environmentConfiguration.getEmbeddedTargetApi()));
            } catch (NoSuchMethodException nsme) {
                return aClass.getConstructor().newInstance();
            }
        } catch (ReflectiveOperationException roe) {
            throw UncheckedException.throwUncheckedException(roe);
        }
    }

    private String getEnvironmentDirectoryPath(ConfigurationParameters cp) {
        return cp.get("chutney.junit.engine.conf.env.path").orElse(CHUTNEY_JUNIT_ENV_PATH);
    }

    private EnvironmentDto getEnvironment(ConfigurationParameters cp) {
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(getEnvironmentDirectoryPath(cp));
        String environmentName = getEnvironmentName(cp);
        return cp.get("chutney.junit.engine.conf.env.name")
            .map(name -> environmentConfiguration.getEmbeddedEnvironmentApi().getEnvironment(environmentName))
            .map(env -> env.variables.stream().collect(Collectors.toMap(EnvironmentVariableDto::key,EnvironmentVariableDto::value)))
            .map(variables -> new EnvironmentDto(environmentName, variables))
            .orElse(new EnvironmentDto(GlacioAdapter.DEFAULT_ENV, Collections.emptyMap()));
    }

    private String getEnvironmentName(ConfigurationParameters cp) {
        return cp.get("chutney.junit.engine.conf.env.name").orElse(GlacioAdapter.DEFAULT_ENV);
    }
}

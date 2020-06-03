package com.chutneytesting.junit.engine;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.engine.api.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.tools.UncheckedException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.util.Optional;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;

public class ChutneyTestEngine extends HierarchicalTestEngine<ChutneyEngineExecutionContext> {

    private final GlacioAdapterConfiguration glacioAdapterConfiguration;
    private final Object chutneyClass;

    public ChutneyTestEngine() {
        try {
            glacioAdapterConfiguration = new GlacioAdapterConfiguration(new ExecutionConfiguration(), getEnvironmentDirectoryPath(), getAgentNetworkFilePath());
            chutneyClass = findChutneyClass();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Override
    public String getId() {
        return "chutney-junit-engine";
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
        ChutneyEngineDescriptor engineDescriptor = new ChutneyEngineDescriptor(uniqueId, "Chutney", chutneyClass);
        new DiscoverySelectorResolver(glacioAdapterConfiguration.glacioAdapter()).resolveSelectors(engineDiscoveryRequest, engineDescriptor);
        return engineDescriptor;
    }

    @Override
    protected ChutneyEngineExecutionContext createExecutionContext(ExecutionRequest executionRequest) {
        return new ChutneyEngineExecutionContext(glacioAdapterConfiguration.executionConfiguration(), executionRequest);
    }

    private Object findChutneyClass() {
        return ReflectionSupport
            .findAllClassesInPackage("", aClass -> aClass.isAnnotationPresent(Chutney.class), x -> true)
            .stream()
            .findFirst()
            .map(this::newInstance)
            .orElse(null);
    }

    private Object newInstance(Class<?> aClass) {
        try {
            try {
                Constructor<?> constructor = aClass.getConstructor(EnvironmentService.class);
                return constructor.newInstance(new EnvironmentServiceImpl(glacioAdapterConfiguration.environmentService()));
            } catch (NoSuchMethodException nsme) {
                return aClass.newInstance();
            }
        } catch (ReflectiveOperationException roe) {
            throw UncheckedException.throwUncheckedException(roe);
        }
    }

    private String getEnvironmentDirectoryPath() {
        return System.getProperty("chutney.junit.engine.conf.env.path", ".chutney/junit/conf");
    }

    private String getAgentNetworkFilePath() {
        return System.getProperty("chutney.junit.engine.conf.agent.path", ".chutney/junit/conf/endpoints.json");
    }
}

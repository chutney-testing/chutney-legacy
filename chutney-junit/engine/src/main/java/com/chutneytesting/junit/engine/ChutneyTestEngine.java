package com.chutneytesting.junit.engine;

import com.chutneytesting.environment.EnvironmentConfiguration;
import com.chutneytesting.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.junit.api.EnvironmentService;
import com.chutneytesting.tools.UncheckedException;
import java.lang.reflect.Constructor;
import java.util.Optional;
import org.junit.platform.commons.support.ReflectionSupport;
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

    private final GlacioAdapterConfiguration glacioAdapterConfiguration;
    private final Object chutneyClass;

    public ChutneyTestEngine() {
        try {
            glacioAdapterConfiguration = new GlacioAdapterConfiguration(getEnvironmentDirectoryPath());
            chutneyClass = findChutneyClass();
        } catch (Exception e) {
            LOGGER.error("{} instantiation error", getId(), e);
            throw UncheckedException.throwUncheckedException(e);
        }
    }

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
            ChutneyEngineDescriptor engineDescriptor = new ChutneyEngineDescriptor(uniqueId, "Chutney", chutneyClass);
            new DiscoverySelectorResolver(glacioAdapterConfiguration.glacioAdapter()).resolveSelectors(engineDiscoveryRequest, engineDescriptor);
            return engineDescriptor;
        } catch (Exception e) {
            LOGGER.error("{} discovery error", getId(), e);
            throw e;
        }
    }

    @Override
    protected ChutneyEngineExecutionContext createExecutionContext(ExecutionRequest executionRequest) {
        return new ChutneyEngineExecutionContext(glacioAdapterConfiguration.executionConfiguration());
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
                EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(getEnvironmentDirectoryPath());
                return constructor.newInstance(new EnvironmentServiceImpl(environmentConfiguration.getEnvironmentEmbeddedApplication()));
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
}

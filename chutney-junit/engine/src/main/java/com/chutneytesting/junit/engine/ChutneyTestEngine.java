package com.chutneytesting.junit.engine;

import com.chutneytesting.environment.EnvironmentConfiguration;
import com.chutneytesting.glacio.GlacioAdapterConfiguration;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.junit.api.Chutney;
import com.chutneytesting.junit.api.EnvironmentService;
import com.chutneytesting.tools.UncheckedException;
import java.lang.reflect.Constructor;
import java.util.Optional;
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
            return new ChutneyEngineExecutionContext(glacioAdapterConfiguration.executionConfiguration(), getEnvironmentName(cp));
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
                return constructor.newInstance(new EnvironmentServiceImpl(environmentConfiguration.getEmbeddedEnvironmentApi()));
            } catch (NoSuchMethodException nsme) {
                return aClass.getConstructor().newInstance();
            }
        } catch (ReflectiveOperationException roe) {
            throw UncheckedException.throwUncheckedException(roe);
        }
    }

    private String getEnvironmentDirectoryPath(ConfigurationParameters cp) {
        return cp.get("chutney.junit.engine.conf.env.path").orElse(".chutney/junit/conf");
    }

    private String getEnvironmentName(ConfigurationParameters cp) {
        return cp.get("chutney.junit.engine.conf.env.name").orElse(GlacioAdapter.DEFAULT_ENV);
    }
}

package com.chutneytesting.environment;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;

public class EnvironmentConfiguration {

    private final EmbeddedEnvironmentApi environmentEmbeddedApplication;

    public EnvironmentConfiguration(String storeFolderPath) {
        EnvironmentRepository environmentRepository = createEnvironmentRepository(storeFolderPath);
        EnvironmentService environmentService = createEnvironmentService(environmentRepository);
        this.environmentEmbeddedApplication = new EmbeddedEnvironmentApi(environmentService);
    }

    private EnvironmentRepository createEnvironmentRepository(String storeFolderPath) {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }

    private EnvironmentService createEnvironmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    public EmbeddedEnvironmentApi getEnvironmentEmbeddedApplication() {
        return environmentEmbeddedApplication;
    }
}

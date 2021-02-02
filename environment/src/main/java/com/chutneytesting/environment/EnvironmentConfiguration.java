package com.chutneytesting.environment;

import com.chutneytesting.environment.api.EnvironmentEmbeddedApplication;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;

public class EnvironmentConfiguration {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentService environmentService;
    private final EnvironmentEmbeddedApplication environmentEmbeddedApplication;

    public EnvironmentConfiguration(String storeFolderPath) {
        this.environmentRepository = createEnvironmentRepository(storeFolderPath);
        this.environmentService = createEnvironmentService(this.environmentRepository);
        this.environmentEmbeddedApplication = new EnvironmentEmbeddedApplication(environmentService);
    }

    private EnvironmentRepository createEnvironmentRepository(String storeFolderPath) {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }

    private EnvironmentService createEnvironmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    public EnvironmentEmbeddedApplication getEnvironmentEmbeddedApplication() {
        return environmentEmbeddedApplication;
    }
}

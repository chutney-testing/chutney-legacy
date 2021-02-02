package com.chutneytesting.environment;

import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;

public class EnvironmentConfiguration {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentService environmentService;

    public EnvironmentConfiguration(String storeFolderPath) {
        this.environmentRepository = createEnvironmentRepository(storeFolderPath);
        this.environmentService = createEnvironmentService(this.environmentRepository);
    }

    private EnvironmentRepository createEnvironmentRepository(String storeFolderPath) {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }

    private EnvironmentService createEnvironmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    public EnvironmentRepository getEnvironmentRepository() {
        return environmentRepository;
    }

    public EnvironmentService getEnvironmentService() {
        return environmentService;
    }
}

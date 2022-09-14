package com.chutneytesting.environment;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;
import com.chutneytesting.environment.infra.MigrateTargetSecurityExecutor;

public class EnvironmentConfiguration {

    private final EnvironmentRepository environmentRepository;
    private final EmbeddedEnvironmentApi environmentApi;

    public EnvironmentConfiguration(String storeFolderPath) {
        this.environmentRepository = createEnvironmentRepository(storeFolderPath);
        EnvironmentService environmentService = createEnvironmentService(environmentRepository);
        this.environmentApi = new EmbeddedEnvironmentApi(environmentService);

        if (environmentRepository.listNames().isEmpty()) {
            createDefaultEnvironment(environmentService);
        }

        migrateTargetSecurity();
    }

    private void createDefaultEnvironment(EnvironmentService environmentService) {
        environmentService.createEnvironment(Environment.builder().withName("DEFAULT").build());
    }

    private void migrateTargetSecurity() {
        if (environmentRepository instanceof JsonFilesEnvironmentRepository) {
            new MigrateTargetSecurityExecutor((JsonFilesEnvironmentRepository) environmentRepository).execute();
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
}

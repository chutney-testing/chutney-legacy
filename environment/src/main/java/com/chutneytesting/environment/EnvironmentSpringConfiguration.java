package com.chutneytesting.environment;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.domain.EnvironmentRepository;
import com.chutneytesting.environment.domain.EnvironmentService;
import com.chutneytesting.environment.infra.JsonFilesEnvironmentRepository;
import java.io.UncheckedIOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentSpringConfiguration {

    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.environment.configuration-folder:~/.chutney/conf/environment}";

    @Bean
    EmbeddedEnvironmentApi environmentEmbeddedApplication(EnvironmentService environmentService) {
        return new EmbeddedEnvironmentApi(environmentService);
    }

    @Bean
    EnvironmentService environmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    @Bean
    JsonFilesEnvironmentRepository jsonFilesEnvironmentRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) throws UncheckedIOException {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }
}

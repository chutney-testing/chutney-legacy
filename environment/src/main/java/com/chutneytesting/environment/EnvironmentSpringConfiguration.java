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

    @Bean
    EmbeddedEnvironmentApi environmentEmbeddedApplication(EnvironmentService environmentService) {
        return new EmbeddedEnvironmentApi(environmentService);
    }

    @Bean
    EnvironmentService environmentService(EnvironmentRepository environmentRepository) {
        return new EnvironmentService(environmentRepository);
    }

    @Bean
    JsonFilesEnvironmentRepository jsonFilesEnvironmentRepository(@Value("${chutney.configuration-folder:~/.chutney/conf}") String storeFolderPath) throws UncheckedIOException {
        return new JsonFilesEnvironmentRepository(storeFolderPath);
    }
}

package com.chutneytesting.environment;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentSpringConfiguration {

    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.environment.configuration-folder:~/.chutney/conf/environment}";

    @Bean
    EmbeddedEnvironmentApi environmentEmbeddedApplication(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) {
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(storeFolderPath);
        return environmentConfiguration.getEmbeddedEnvironmentApi();
    }
}

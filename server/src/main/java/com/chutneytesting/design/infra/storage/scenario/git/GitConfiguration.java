package com.chutneytesting.design.infra.storage.scenario.git;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chutneytesting.design.infra.storage.scenario.jdbc.TestCaseData;
import com.chutneytesting.design.infra.storage.scenario.git.json.versionned.JsonMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitConfiguration {

    @Bean
    JsonMapper<TestCaseData> TestCaseDataJsonMapper(@Qualifier("persistenceObjectMapper") ObjectMapper objectMapper) {
        return new JsonMapper<>("1", TestCaseData.class, objectMapper);
    }
}

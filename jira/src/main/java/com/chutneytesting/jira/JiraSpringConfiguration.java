package com.chutneytesting.jira;

import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.HttpJiraXrayImpl;
import com.chutneytesting.jira.infra.JiraFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiraSpringConfiguration {

    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.environment.configuration-folder:~/.chutney/conf}";

    // api Bean
    @Bean
    JiraXrayEmbeddedApi jiraXrayEmbeddedApi(JiraXrayService jiraXrayService) {
        return new JiraXrayEmbeddedApi(jiraXrayService);
    }

    // domain Bean
    @Bean
    JiraXrayService jiraXrayService(JiraRepository jiraRepository, JiraXrayApi jiraXrayApi) {
        return new JiraXrayService(jiraRepository, jiraXrayApi);
    }

    // infra Bean
    @Bean
    JiraRepository jiraFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) {
        return new JiraFileRepository(storeFolderPath);
    }

    @Bean
    JiraXrayApi httpJiraXrayApi() {
        return new HttpJiraXrayImpl();
    }
}

/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.jira;

import com.chutneytesting.jira.api.JiraXrayEmbeddedApi;
import com.chutneytesting.jira.domain.JiraRepository;
import com.chutneytesting.jira.domain.JiraXrayClientFactory;
import com.chutneytesting.jira.domain.JiraXrayService;
import com.chutneytesting.jira.infra.JiraFileRepository;
import com.chutneytesting.jira.infra.JiraXrayFactoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiraSpringConfiguration {

    public static final String CONFIGURATION_FOLDER_SPRING_VALUE = "${chutney.jira.configuration-folder:~/.chutney/conf/jira}";

    // api Bean
    @Bean
    JiraXrayEmbeddedApi jiraXrayEmbeddedApi(JiraXrayService jiraXrayService) {
        return new JiraXrayEmbeddedApi(jiraXrayService);
    }

    // domain Bean
    @Bean
    JiraXrayService jiraXrayService(JiraRepository jiraRepository, JiraXrayClientFactory jiraXrayFactory) {
        return new JiraXrayService(jiraRepository, jiraXrayFactory);
    }

    // infra Bean
    @Bean
    JiraXrayClientFactory jiraXrayFactory() {
        return new JiraXrayFactoryImpl();
    }

    @Bean
    JiraRepository jiraFileRepository(@Value(CONFIGURATION_FOLDER_SPRING_VALUE) String storeFolderPath) {
        return new JiraFileRepository(storeFolderPath);
    }
}

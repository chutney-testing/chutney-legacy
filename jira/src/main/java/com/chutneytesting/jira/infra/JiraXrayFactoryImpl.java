package com.chutneytesting.jira.infra;

import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayClientFactory;

public class JiraXrayFactoryImpl implements JiraXrayClientFactory {

    @Override
    public JiraXrayApi create(JiraTargetConfiguration jiraTargetConfiguration) {
        return new HttpJiraXrayImpl(jiraTargetConfiguration);
    }

}

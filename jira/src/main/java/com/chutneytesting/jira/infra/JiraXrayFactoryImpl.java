package com.chutneytesting.jira.infra;

import com.chutneytesting.jira.domain.JiraTargetConfiguration;
import com.chutneytesting.jira.domain.JiraXrayApi;
import com.chutneytesting.jira.domain.JiraXrayFactory;

public class JiraXrayFactoryImpl implements JiraXrayFactory {

    @Override
    public JiraXrayApi createHttpJiraXrayImpl(JiraTargetConfiguration jiraTargetConfiguration) {
        return new HttpJiraXrayImpl(jiraTargetConfiguration);
    }

}

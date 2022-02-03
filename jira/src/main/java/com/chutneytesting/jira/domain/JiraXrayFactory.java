package com.chutneytesting.jira.domain;

public interface JiraXrayFactory {

    JiraXrayApi createHttpJiraXrayImpl(JiraTargetConfiguration jiraTargetConfiguration);

}

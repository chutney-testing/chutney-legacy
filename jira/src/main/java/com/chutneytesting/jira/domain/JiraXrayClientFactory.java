package com.chutneytesting.jira.domain;

public interface JiraXrayClientFactory {

    JiraXrayApi create(JiraTargetConfiguration jiraTargetConfiguration);

}

package com.chutneytesting.jira.domain.exception;

public class NoJiraConfigurationException extends RuntimeException {

    public NoJiraConfigurationException() {
        super("Cannot request xray server, jira url is undefined");
    }

}

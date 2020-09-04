package com.chutneytesting.design.domain.jira;

@SuppressWarnings("serial")
public class JiraConfigurationNotFoundException extends RuntimeException {
    public JiraConfigurationNotFoundException(String message) {
        super(message);
    }
}

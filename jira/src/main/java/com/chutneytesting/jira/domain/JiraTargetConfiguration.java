package com.chutneytesting.jira.domain;

public class JiraTargetConfiguration {
    public final String url;
    public final String username;
    public final String password;

    public JiraTargetConfiguration(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}

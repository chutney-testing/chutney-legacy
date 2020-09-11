package com.chutneytesting.design.domain.jira;

public class JiraTargetConfiguration {
    public final String url;
    public final String username;
    public final String password;

    public JiraTargetConfiguration() {
        this("", "", "");
    }

    public JiraTargetConfiguration(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}

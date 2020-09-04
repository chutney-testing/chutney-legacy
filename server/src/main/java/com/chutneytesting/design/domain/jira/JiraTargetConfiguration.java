package com.chutneytesting.design.domain.jira;

public class JiraTargetConfiguration {
    public String url;
    public String username;
    public String password;

    public JiraTargetConfiguration() {
    }

    public JiraTargetConfiguration(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}

package com.chutneytesting.jira.infra;

public class JiraTargetConfigurationDto {
    public final String url;
    public final String username;
    public final String password;

    public JiraTargetConfigurationDto() {
        this("", "", "");
    }

    public JiraTargetConfigurationDto(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}

package com.chutneytesting.jira.domain;

import org.apache.commons.lang3.StringUtils;

public class JiraTargetConfiguration {

    public final String url;
    public final String username;
    public final String password;

    public JiraTargetConfiguration(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(url);
    }
}

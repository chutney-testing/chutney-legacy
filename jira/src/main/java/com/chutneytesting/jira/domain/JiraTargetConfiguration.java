package com.chutneytesting.jira.domain;

import org.apache.commons.lang3.StringUtils;

public record JiraTargetConfiguration(String url, String username, String password) {

    public boolean isValid() {
        return StringUtils.isNotEmpty(url);
    }
}

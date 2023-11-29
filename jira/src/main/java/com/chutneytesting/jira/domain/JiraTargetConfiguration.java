package com.chutneytesting.jira.domain;

import org.apache.commons.lang3.StringUtils;

public record JiraTargetConfiguration(String url, String username, String password, String urlProxy, String userProxy,
                                      String passwordProxy) {
    public boolean isValid() {
        return StringUtils.isNotEmpty(url);
    }

    public boolean hasProxy() {
        return StringUtils.isNotEmpty(urlProxy);
    }

    public boolean hasProxyWithAuth() {
        return StringUtils.isNotEmpty(urlProxy) &&
            StringUtils.isNotEmpty(userProxy) &&
            StringUtils.isNotEmpty(passwordProxy);
    }
}

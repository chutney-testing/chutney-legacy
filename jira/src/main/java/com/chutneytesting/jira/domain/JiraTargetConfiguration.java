package com.chutneytesting.jira.domain;

import org.apache.commons.lang3.StringUtils;

public record JiraTargetConfiguration(String url, String username, String password, String urlProxy, String userProxy,
                                      String passwordProxy) {
    public boolean isValid() {
        return StringUtils.isNotBlank(url);
    }

    public boolean hasProxy() {
        return StringUtils.isNotBlank(urlProxy);
    }

    public boolean hasProxyWithAuth() {
        return StringUtils.isNotBlank(urlProxy) &&
            StringUtils.isNotBlank(userProxy) &&
            StringUtils.isNotBlank(passwordProxy);
    }
}

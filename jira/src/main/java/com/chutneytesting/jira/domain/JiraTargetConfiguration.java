package com.chutneytesting.jira.domain;

import org.apache.commons.lang3.StringUtils;

public record JiraTargetConfiguration(String url, String username, String password, String urlProxy, String userProxy,
                                      Integer portProxy,
                                      String passwordProxy) {

    public boolean isValid() {
        return StringUtils.isNotEmpty(url);
    }

    public boolean hasProxy() {
        return portProxy != null &&
            StringUtils.isNotEmpty(urlProxy) &&
            StringUtils.isNotEmpty(userProxy) &&
            StringUtils.isNotEmpty(passwordProxy);
    }
}

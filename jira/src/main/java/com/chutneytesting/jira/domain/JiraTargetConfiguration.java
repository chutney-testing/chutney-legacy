/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.jira.domain;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;

public record JiraTargetConfiguration(
    String url, String username, String password,
    String urlProxy, String userProxy, String passwordProxy
) {
    public boolean isValid() {
        return isNotBlank(url);
    }

    public boolean hasProxy() {
        return isNotBlank(urlProxy);
    }

    public boolean hasProxyWithAuth() {
        return isNotBlank(urlProxy) &&
            isNotBlank(userProxy) &&
            isNotBlank(passwordProxy);
    }
}

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

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

package com.chutneytesting.jira.xrayapi;

public class JiraIssueType {
    private String self;
    private Long id;
    private String name;
    private boolean subtask;
    private String description;
    private String iconUri;

    public JiraIssueType() {
    }

    public JiraIssueType(String self, Long id, String name, boolean subtask, String description, String iconUri) {
        this.self = self;
        this.id = id;
        this.name = name;
        this.subtask = subtask;
        this.description = description;
        this.iconUri = iconUri;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSubtask() {
        return subtask;
    }

    public void setSubtask(boolean subtask) {
        this.subtask = subtask;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }
}

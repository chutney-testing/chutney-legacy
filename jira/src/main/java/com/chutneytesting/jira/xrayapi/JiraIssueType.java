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

package com.chutneytesting.admin.api.dto;

import java.time.LocalDateTime;

public class BackupDto {

    private final LocalDateTime time;
    private final Boolean homePage;
    private final Boolean agentsNetwork;
    private final Boolean environments;
    private final Boolean components;
    private final Boolean globalVars;
    private final Boolean jiraLinks;

    public BackupDto(LocalDateTime time,
                     Boolean homePage,
                     Boolean agentsNetwork,
                     Boolean environments,
                     Boolean components,
                     Boolean globalVars,
                     Boolean jiraLinks) {
        this.time = time;
        this.homePage = homePage;
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
        this.jiraLinks = jiraLinks;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Boolean getHomePage() {
        return homePage;
    }

    public Boolean getAgentsNetwork() {
        return agentsNetwork;
    }

    public Boolean getEnvironments() {
        return environments;
    }

    public Boolean getComponents() {
        return components;
    }

    public Boolean getGlobalVars() {
        return globalVars;
    }

    public Boolean getJiraLinks() {
        return jiraLinks;
    }
}

package com.chutneytesting.admin.api.dto;

import java.time.LocalDateTime;

public class BackupDto {

    private final LocalDateTime time;
    private final boolean agentsNetwork;
    private final boolean environments;
    private final boolean components;
    private final boolean globalVars;
    private final boolean jiraLinks;

    public BackupDto(LocalDateTime time,
                     boolean agentsNetwork,
                     boolean environments,
                     boolean components,
                     boolean globalVars,
                     boolean jiraLinks) {
        this.time = time;
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
        this.jiraLinks = jiraLinks;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public boolean getAgentsNetwork() {
        return agentsNetwork;
    }

    public boolean getEnvironments() {
        return environments;
    }

    public boolean getComponents() {
        return components;
    }

    public boolean getGlobalVars() {
        return globalVars;
    }

    public boolean getJiraLinks() {
        return jiraLinks;
    }
}

package com.chutneytesting.admin.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BackupDto {

    private final LocalDateTime time;
    private final Boolean homePage;
    private final Boolean agentsNetwork;
    private final Boolean environments;
    private final Boolean components;
    private final Boolean globalVars;

    public BackupDto(LocalDateTime time,
                     Boolean homePage,
                     Boolean agentsNetwork,
                     Boolean environments,
                     Boolean components,
                     Boolean globalVars) {
        this.time = time;
        this.homePage = homePage;
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
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
}

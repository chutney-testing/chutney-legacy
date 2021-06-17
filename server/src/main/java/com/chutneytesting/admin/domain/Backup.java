package com.chutneytesting.admin.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Backup {

    public final static DateTimeFormatter backupIdTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public final LocalDateTime time;
    public final Boolean homePage;
    public final Boolean agentsNetwork;
    public final Boolean environments;
    public final Boolean components;
    public final Boolean globalVars;
    public final Boolean jiraLinks;

    public Backup(Boolean homePage, Boolean agentsNetwork, Boolean environments, Boolean components, Boolean globalVars, Boolean jiraLinks) {
        if (!(homePage || agentsNetwork || environments || components || globalVars || jiraLinks)) {
            throw new IllegalArgumentException("Nothing to backup !!");
        }

        this.time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.homePage = homePage;
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
        this.jiraLinks = jiraLinks;
    }

    public Backup(String id, Boolean homePage, Boolean agentsNetwork, Boolean environments, Boolean components, Boolean globalVars, Boolean jiraLinks) {
        this.time = LocalDateTime.parse(id, backupIdTimeFormatter);
        this.homePage = homePage;
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
        this.jiraLinks = jiraLinks;
    }

    public String id() {
        return this.time.format(backupIdTimeFormatter);
    }
}

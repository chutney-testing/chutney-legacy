package com.chutneytesting.admin.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Backup {

    public final static DateTimeFormatter backupIdTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public final LocalDateTime time;
    public final boolean agentsNetwork;
    public final boolean environments;
    public final boolean components;
    public final boolean globalVars;
    public final boolean jiraLinks;

    public Backup(boolean agentsNetwork, boolean environments, boolean components, boolean globalVars, boolean jiraLinks) {
        if (!(agentsNetwork || environments || components || globalVars || jiraLinks)) {
            throw new IllegalArgumentException("Nothing to backup !!");
        }

        this.time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        this.agentsNetwork = agentsNetwork;
        this.environments = environments;
        this.components = components;
        this.globalVars = globalVars;
        this.jiraLinks = jiraLinks;
    }

    public Backup(String id, Boolean agentsNetwork, Boolean environments, Boolean components, Boolean globalVars, Boolean jiraLinks) {
        this.time = LocalDateTime.parse(id, backupIdTimeFormatter);
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

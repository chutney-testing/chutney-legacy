package com.chutneytesting.admin.api.dto;

import com.chutneytesting.admin.domain.Backup;

public class BackupMapper {

    public static Backup fromDto(BackupDto dto) {
        return new Backup(dto.getHomePage(), dto.getAgentsNetwork(), dto.getEnvironments(), dto.getComponents(), dto.getGlobalVars(), dto.getJiraLinks());
    }

    public static BackupDto toDto(Backup backup) {
        return new BackupDto(backup.time, backup.homePage, backup.agentsNetwork, backup.environments, backup.components, backup.globalVars, backup.jiraLinks);
    }
}

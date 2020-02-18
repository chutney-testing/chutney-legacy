package com.chutneytesting.admin.domain;

@SuppressWarnings("serial")
public class BackupNotFoundException extends RuntimeException {

    public BackupNotFoundException(String backupId) {
        super("Backup [" + backupId + "] not found !");
    }

}

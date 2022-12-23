package com.chutneytesting.admin.domain;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface BackupRepository {

    String save(Backup backup);
    Backup read(String backupId);
    void delete(String backupId);
    List<Backup> list();
    void getBackupData(String backupId, OutputStream outputStream) throws IOException;

    List<String> getBackupables();
}

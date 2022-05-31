package com.chutneytesting.admin.domain;

import java.io.OutputStream;

public interface Backupable {
    void backup(OutputStream outputStream);
}

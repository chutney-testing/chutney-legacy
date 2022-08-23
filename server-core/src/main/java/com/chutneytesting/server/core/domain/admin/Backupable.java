package com.chutneytesting.server.core.domain.admin;

import java.io.OutputStream;

public interface Backupable {
    void backup(OutputStream outputStream);
}

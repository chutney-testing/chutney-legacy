package com.chutneytesting.server.core.admin;

import java.io.OutputStream;

public interface Backupable {
    void backup(OutputStream outputStream);
}

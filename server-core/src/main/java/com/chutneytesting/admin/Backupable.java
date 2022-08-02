package com.chutneytesting.admin;

import java.io.OutputStream;

public interface Backupable {
    void backup(OutputStream outputStream);
}

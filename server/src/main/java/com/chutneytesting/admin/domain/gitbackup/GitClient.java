package com.chutneytesting.admin.domain.gitbackup;

import java.nio.file.Path;

public interface GitClient {

    boolean hasAccess(RemoteRepository remote);

    void clone(RemoteRepository remote, Path cloningPath);

    void update(RemoteRepository remote, Path workingDirectory);

    void commit(Path workingDirectory, String message);

    void push(RemoteRepository remote, Path workingDirectory);

    void addAll(Path workingDirectory);

}

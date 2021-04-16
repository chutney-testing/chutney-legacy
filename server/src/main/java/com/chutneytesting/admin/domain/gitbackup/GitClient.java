package com.chutneytesting.admin.domain.gitbackup;

import java.nio.file.Path;

public interface GitClient {

    boolean hasAccess(RemoteRepository remote);

    boolean isGitDir(Path workingDirectory);

    void addAll(Path workingDirectory);

    void clone(RemoteRepository remote, Path cloningPath);

    void commit(Path workingDirectory, String message);

    void createBranch(RemoteRepository remote, Path workingDirectory);

    void initRepository(RemoteRepository remote, Path workingDirectory);

    void push(RemoteRepository remote, Path workingDirectory);

    void update(RemoteRepository remote, Path workingDirectory);

    void updateRemote(RemoteRepository remote, Path workingDirectory);

}

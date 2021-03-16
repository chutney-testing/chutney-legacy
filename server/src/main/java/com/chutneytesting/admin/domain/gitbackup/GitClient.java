package com.chutneytesting.admin.domain.gitbackup;

public interface GitClient {

    boolean hasAccess(RemoteRepository remote);

}

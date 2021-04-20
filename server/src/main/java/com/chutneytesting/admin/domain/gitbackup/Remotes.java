package com.chutneytesting.admin.domain.gitbackup;

import java.util.List;

public interface Remotes {

    List<RemoteRepository> getAll();

    RemoteRepository add(RemoteRepository remoteRepository);

    void remove(String id);

    RemoteRepository get(String name);

}

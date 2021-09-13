package com.chutneytesting.admin.domain.gitbackup;

import java.util.List;

public interface BackupService<T> {

    List<T> repositories();

    T add(T repository);

    void remove(String name);

    void export();

    void export(String name);

    void export(T remote);

    void importFrom(String name);

    void importFrom(T remote);
}

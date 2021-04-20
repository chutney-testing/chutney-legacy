package com.chutneytesting.admin.domain.gitbackup;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class RemoteRepository {

    public final String name;
    public final String url;
    public final String branch;

    public final String privateKeyPath;
    public final String privateKeyPassphrase;

    public RemoteRepository(String name, String url, String branch, String privateKeyPath, String privateKeyPassphrase) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("You must provide a remote name");
        }

        this.name = name;
        this.url = url;
        this.branch = branch;
        this.privateKeyPath = privateKeyPath;
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteRepository remoteRepository = (RemoteRepository) o;
        return Objects.equals(name, remoteRepository.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

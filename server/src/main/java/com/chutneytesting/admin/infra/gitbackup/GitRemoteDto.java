package com.chutneytesting.admin.infra.gitbackup;

public class GitRemoteDto {

    public final String name;
    public final String url;
    public final String branch;
    public final String privateKeyPath;
    public final String privateKeyPassphrase;

    public GitRemoteDto() {
        this("", "", "", "", "");
    }

    public GitRemoteDto(String name, String url, String branch, String privateKeyPath, String privateKeyPassphrase) {
        this.name = name;
        this.url = url;
        this.branch = branch;
        this.privateKeyPath = privateKeyPath;
        this.privateKeyPassphrase = privateKeyPassphrase;
    }
}

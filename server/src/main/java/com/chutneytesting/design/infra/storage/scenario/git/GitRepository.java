package com.chutneytesting.design.infra.storage.scenario.git;

public class GitRepository {

    public final Long id;
    public final String url;
    public final String testSubFolder;
    /** Define the origin of the scenarios */
    public final String repositoryName;

    /**
     * For jackson
     */
    public GitRepository() {
        this(-1L, "", "", "");
    }

    public GitRepository(Long id, String url, String testSubFolder, String repositoryName) {
        this.id = id;
        this.url = url;
        this.testSubFolder = testSubFolder;
        this.repositoryName = repositoryName;
    }

    @Override
    public String toString() {
        return "GitRepository{" +
            "id=" + id +
            ", url='" + url + '\'' +
            ", testSubFolder='" + testSubFolder + '\'' +
            ", repositoryName='" + repositoryName + '\'' +
            '}';
    }
}

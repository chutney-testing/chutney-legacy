package com.chutneytesting.admin.infra.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.infra.storage.scenario.git.GitRepository;
import java.nio.file.Files;
import org.junit.Test;

public class JsonFilesGitRepositoryTest {

    @Test
    public void save_list_save_list_delete_list() throws Exception {
        String tmpPath = Files.createTempDirectory("git-test").toAbsolutePath().toString();
        JsonFilesGitRepository service = new JsonFilesGitRepository(tmpPath);

        assertThat(service.listGitRepository()).isEmpty();

        GitRepository gitRepo1 = new GitRepository(1L, "url", "testSubFolder", "repoName");
        service.save(gitRepo1);

        assertThat(service.listGitRepository()).hasSize(1);
        GitRepository next = service.listGitRepository().iterator().next();
        assertThat(next.id).isEqualTo(1L);
        assertThat(next.url).isEqualTo("url");
        assertThat(next.testSubFolder).isEqualTo("testSubFolder");
        assertThat(next.repositoryName).isEqualTo("repoName");

        GitRepository gitRepo2 = new GitRepository(2L, "url2", "testSubFolder2", "repoName2");
        service.save(gitRepo2);

        assertThat(service.listGitRepository()).hasSize(2);

        service.delete(gitRepo1.id);

        assertThat(service.listGitRepository()).hasSize(1);
        next = service.listGitRepository().iterator().next();
        assertThat(next.id).isEqualTo(2L);
        assertThat(next.url).isEqualTo("url2");
        assertThat(next.testSubFolder).isEqualTo("testSubFolder2");
        assertThat(next.repositoryName).isEqualTo("repoName2");
    }

}

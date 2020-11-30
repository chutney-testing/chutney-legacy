package com.chutneytesting.design.infra.storage.scenario.git;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GitClientTest {

    Path pathTest;
    String repoName = "testGitClient";
    Git git;

    @BeforeEach
    public void setUp() throws Exception {
        pathTest = new GitClient().getGitDirectory(repoName);
        tearDown();

        Files.createDirectories(pathTest);

        git = Git.init().setDirectory(pathTest.toFile()).call();

    }

    @AfterEach
    public void tearDown() throws Exception {
        if(pathTest.toFile().exists()) {
            try(Stream<Path> stream = Files.walk(pathTest)) {
                stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
    }

    @Test
    public void add_and_commit() throws Exception {
        GitClient client = new GitClient();

        Path file = Files.createFile(pathTest.resolve("test_add.txt"));
        assertThat(git.status().call().getUntracked().contains(file.toFile().getName())).isTrue() ;

        client.addCommitPushFile(repoName, "commit message for test");
        assertThat(git.status().call().getUntracked()).hasSize(0);

        Iterator<RevCommit> commits = git.log().call().iterator();
        assertThat(commits.hasNext()).isTrue();

        assertThat(commits.next().getFullMessage()).isEqualTo("commit message for test");
        assertThat(commits.hasNext()).isFalse();
    }

    @Test
    public void remove_and_commit() throws Exception {
        GitClient client = new GitClient();

        Path file = Files.createFile(pathTest.resolve("test_rm.txt"));
        client.addCommitPushFile(repoName, "commit message for test");

        Files.delete(file);
        client.removeCommitPushFile(repoName, "remove file", "test_rm.txt");

    }

    @Test
    public void directory_should_be_in_user_home() throws Exception {
        GitClient client = new GitClient();
        Path gitDir = client.getGitDirectory("toto");
        assertThat(gitDir).isEqualByComparingTo(Paths.get(System.getProperty("user.home")).resolve("toto"));
    }
}

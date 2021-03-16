package com.chutneytesting.admin.infra.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import com.chutneytesting.admin.domain.gitbackup.Remotes;
import com.chutneytesting.tools.file.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GitRemoteFileRepositoryTest {

    private static Remotes sut;
    private static Path REMOTES_FILE;

    @BeforeAll
    public static void setUp(@TempDir Path temporaryFolder) {
        String tmpConfDir = temporaryFolder.toFile().getAbsolutePath();
        System.setProperty("configuration-folder", tmpConfDir);
        System.setProperty("persistence-repository-folder", tmpConfDir);

        sut = new RemotesFileRepository(tmpConfDir);
        REMOTES_FILE = Paths.get(tmpConfDir + "/plugins/git-remotes.json");
    }

    @Test
    public void should_save_a_remote() {
        // Given
        RemoteRepository remote = new RemoteRepository("origin", "git@github.com:chutney-testing/chutney.git", "backup", "private_key", "passphrase");
        String expected =
            "{\n" +
            "  \"origin\" : {\n" +
            "    \"name\" : \"origin\",\n" +
            "    \"url\" : \"git@github.com:chutney-testing/chutney.git\",\n" +
            "    \"branch\" : \"backup\",\n" +
            "    \"privateKeyPath\" : \"private_key\",\n" +
            "    \"privateKeyPassphrase\" : \"passphrase\"\n" +
            "  }\n" +
            "}";

        // When
        sut.add(remote);

        // Then
        String actualContent = FileUtils.readContent(REMOTES_FILE);

        assertThat(actualContent).isEqualToIgnoringNewLines(expected);
    }

    @Test
    public void should_add_a_remote() {
        // Given
        FileUtils.writeContent(REMOTES_FILE,
            "{\n" +
            "  \"origin\" : {\n" +
            "    \"name\" : \"origin\",\n" +
            "    \"url\" : \"git@github.com:chutney-testing/chutney.git\",\n" +
            "    \"branch\" : \"backup\",\n" +
            "    \"privateKeyPath\" : \"private_key\",\n" +
            "    \"privateKeyPassphrase\" : \"passphrase\"\n" +
            "  }\n" +
            "}"
        );
        RemoteRepository remote = new RemoteRepository("name", "url", "branch", "private_key", "passphrase");
        String expected =
            "{\n" +
            "  \"name\" : {\n" +
            "    \"name\" : \"name\",\n" +
            "    \"url\" : \"url\",\n" +
            "    \"branch\" : \"branch\",\n" +
            "    \"privateKeyPath\" : \"private_key\",\n" +
            "    \"privateKeyPassphrase\" : \"passphrase\"\n" +
            "  },\n" +
            "  \"origin\" : {\n" +
            "    \"name\" : \"origin\",\n" +
            "    \"url\" : \"git@github.com:chutney-testing/chutney.git\",\n" +
            "    \"branch\" : \"backup\",\n" +
            "    \"privateKeyPath\" : \"private_key\",\n" +
            "    \"privateKeyPassphrase\" : \"passphrase\"\n" +
            "  }\n" +
            "}";

        // When
        sut.add(remote);

        // Then
        String actualContent = FileUtils.readContent(REMOTES_FILE);

        assertThat(actualContent).isEqualToIgnoringNewLines(expected);
    }

    @Test
    public void should_remove_a_remote() {
        // Given
        FileUtils.writeContent(REMOTES_FILE,
            "{\n" +
            "  \"origin\" : {\n" +
            "    \"name\" : \"origin\",\n" +
            "    \"url\" : \"url\",\n" +
            "    \"branch\" : \"branch\",\n" +
            "    \"privateKeyPath\" : \"public_key\",\n" +
            "    \"privateKeyPassphrase\" : \"passphrase\"\n" +
            "  }\n" +
            "}"
        );

        // When
        sut.remove("origin");

        // Then
        String actualContent = FileUtils.readContent(REMOTES_FILE);
        assertThat(actualContent).isEqualToIgnoringNewLines("{ }");
    }

    @Test
    public void should_read_all_remotes() {
        // Given
        RemoteRepository remote_1 = new RemoteRepository("name", "uri", "branch", "private_key", "passphrase");
        RemoteRepository remote_2 = new RemoteRepository("origin", "git@github.com:chutney-testing/chutney.git", "backup", "key", "passphrase");

        sut.add(remote_1);
        sut.add(remote_2);

        // When
        List<RemoteRepository> remotes = sut.getAll();

        // Then
        assertThat(remotes).hasSize(2);
        assertThat(remotes).contains(remote_1, remote_2);
    }

}

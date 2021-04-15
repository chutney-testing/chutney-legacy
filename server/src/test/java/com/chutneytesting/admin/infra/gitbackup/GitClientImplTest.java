package com.chutneytesting.admin.infra.gitbackup;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.admin.domain.gitbackup.GitClient;
import com.chutneytesting.admin.domain.gitbackup.RemoteRepository;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GitClientImplTest {

    private static final String privateKeyPath = "private/Key/Path";
    private static final String passphrase = "";

    private final GitClient sut = new GitClientImpl();

    @TempDir
    static Path temporaryFolder;

    private static Stream<Arguments> unreachableRemotes() {
        return Stream.of(
            Arguments.of("unreachable", "https://not.reachable.org/repo.git",             "branch", privateKeyPath, passphrase),
            Arguments.of("private",     "https://github.com/chutney-testing/private.git", "master", privateKeyPath, passphrase),
            Arguments.of("wrongBranch", "https://github.com/chutney-testing/chutney.git", "wrongBranch", privateKeyPath, passphrase)
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("unreachableRemotes")
    void should_return_false_when_remote_is_not_accessible(String name, String uri, String branch, String privateKeyPath, String privateKeyPassphrase) {
        RemoteRepository remote = new RemoteRepository(name, uri, branch, privateKeyPath, privateKeyPassphrase);
        boolean actual = sut.hasAccess(remote);
        assertThat(actual).isFalse();
    }

    private static Stream<Arguments> reachableRemotes() {
        return Stream.of(
            Arguments.of("origin", "git@github.com:chutney-testing/private.git", "master", privateKeyPath, passphrase),
            Arguments.of("origin", "git@github.com:chutney-testing/chutney.git", "master", privateKeyPath, passphrase)
        );
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("reachableRemotes")
    void should_return_true_when_remote_is_accessible(String name, String uri, String branch, String privateKeyPath, String privateKeyPassphrase) {
        RemoteRepository remote = new RemoteRepository(name, uri, branch, privateKeyPath, privateKeyPassphrase);
        boolean actual = sut.hasAccess(remote);
        assertThat(actual).isTrue();
    }

    @Test
    void should_create_folders_when_cloning() {
        // Given
        RemoteRepository remote = new RemoteRepository("name", "uri", "branch", "privateKeyPath", "privateKeyPassphrase");

        // When
        try {
            sut.clone(remote, temporaryFolder.resolve("created"));
        }
        catch (Exception e) {
            // do nothing
        }

        // Then
        assertThat(temporaryFolder.resolve("created").toFile().exists()).isTrue();
        assertThat(temporaryFolder.resolve("created").toFile().isDirectory()).isTrue();
    }

}

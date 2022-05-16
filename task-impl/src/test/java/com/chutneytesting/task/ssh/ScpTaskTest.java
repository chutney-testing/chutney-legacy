package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.fakes.FakeServerSsh;
import com.chutneytesting.task.ssh.fakes.FakeTargetInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ScpTaskTest {

    @TempDir
    static Path temporaryFolder;
    private static SshServer credentialSshServer;
    private static SshServer keySshServer;

    @BeforeAll
    public static void setUp() throws IOException {
        credentialSshServer = FakeServerSsh.buildLocalSshServer(false, true, 1);
        keySshServer = FakeServerSsh.buildLocalSshServer(false, false, 2, "src/test/resources/security/authorized_keys"); // MAX_AUTH_REQUEST set to 2 due to session.auth().verify() performing 2 auth requests instead of 1
        credentialSshServer.start();
        keySshServer.start();
    }

    @AfterAll
    public static void stop_ssh_server() throws Exception {
        credentialSshServer.stop();
        keySshServer.stop();
    }

    @ParameterizedTest
    @MethodSource("securedTargets")
    void should_upload_local_file_to_remote_destination(Target target) {
        // Given
        String srcFile = ScpTaskTest.class.getResource(ScpTaskTest.class.getSimpleName() + ".class").getPath();
        String dstFile = temporaryFolder.toString();
        Path expectedFile = temporaryFolder.resolve(ScpTaskTest.class.getSimpleName() + ".class");

        ScpUploadTask task = new ScpUploadTask(target, mock(Logger.class), srcFile, dstFile, "10 s");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("securedTargets")
    void should_download_remote_file_to_local_destination(Target target) {
        // Given
        String srcFile = ScpTaskTest.class.getResource(ScpTaskTest.class.getSimpleName() + ".class").getPath();
        String dstFile = temporaryFolder.resolve("downloaded").toString();
        Path expectedFile = temporaryFolder.resolve("downloaded");

        ScpDownloadTask task = new ScpDownloadTask(target, mock(Logger.class), srcFile, dstFile, "10 s");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat(Files.exists(expectedFile)).isTrue();
    }

    public static List<Arguments> securedTargets() {
        return List.of(
            Arguments.of(FakeTargetInfo.buildTargetWithPassword(credentialSshServer)),
            Arguments.of(FakeTargetInfo.buildTargetWithPrivateKeyWithoutPassphrase(keySshServer)),
            Arguments.of(FakeTargetInfo.buildTargetWithPrivateKeyWithPassphrase(keySshServer))
        );
    }

}

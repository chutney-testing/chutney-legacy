package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static com.chutneytesting.task.ssh.fakes.FakeServerSsh.buildLocalSftpServer;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.fakes.FakeTargetInfo;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.sshd.server.SshServer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SftpTaskTest {

    @TempDir
    static Path temporaryFolder;
    private static SshServer sftpServer;

    @BeforeAll
    public static void setUp() throws IOException {
        sftpServer = buildLocalSftpServer(false, true, 1);
        sftpServer.start();
    }

    @AfterAll
    public static void stop_ssh_server() throws Exception {
        sftpServer.stop();
    }

    @Test
    public void should_list_directory_files() {
        // Given
        Target target = FakeTargetInfo.buildTargetWithCredentialUsernamePassword(sftpServer);
        String directory = ScpTaskTest.class.getResource("/security").getPath();
        List<String> expectedFiles = Lists.newArrayList(
            "client_rsa.pub",
            "authorized_keys",
            "client_ecdsa.pub",
            "keystore-with-keypwd.jks",
            "client_rsa.key",
            "client_ecdsa.key",
            "server.jks",
            "truststore.jks"
        );

        SftpListDirTask task = new SftpListDirTask(target, new TestLogger(), directory, "1 h");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat((List<String>)actualResult.outputs.get("files")).hasSameElementsAs(expectedFiles);
    }

}

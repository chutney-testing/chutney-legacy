package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static com.chutneytesting.task.ssh.fakes.FakeServerSsh.buildLocalSftpServer;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.fakes.FakeTargetInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.sshd.server.SshServer;
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
    public void wip_upload() {
        // Given
        Target target = FakeTargetInfo.buildTargetWithCredentialUsernamePassword(sftpServer);
        String srcFile = ScpTaskTest.class.getResource("/sftptest.file.txt").getPath();
        String dstFile = temporaryFolder.toString();
        Path expectedFile = temporaryFolder.resolve(ScpTaskTest.class.getSimpleName() + ".class");

        SftpUploadTask task = new SftpUploadTask(target, new TestLogger(), srcFile, dstFile, "1 h");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat(Files.exists(expectedFile)).isTrue();
    }

}

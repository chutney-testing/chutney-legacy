package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Success;
import static com.chutneytesting.task.ssh.fakes.FakeServerSsh.buildLocalSftpServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.fakes.FakeTargetInfo;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        String directory = SftpTaskTest.class.getResource("/security").getPath();
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

        SftpListDirTask task = new SftpListDirTask(target, new TestLogger(), directory, "1 m");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat((List<String>) actualResult.outputs.get("files")).hasSameElementsAs(expectedFiles);
    }

    @Test
    public void should_get_file_attributes() {
        // Given
        Target target = FakeTargetInfo.buildTargetWithCredentialUsernamePassword(sftpServer);
        String directory = SftpTaskTest.class.getResource("/security").getPath();
        String regularFile = SftpTaskTest.class.getResource("/security/authorized_keys").getPath();
        String[] expectedKeys = {"CreationDate", "lastAccess", "lastModification", "type", "owner:group"};

        SftpFileInfoTask fileTask = new SftpFileInfoTask(target, new TestLogger(), regularFile, "1 m");
        SftpFileInfoTask dirTask = new SftpFileInfoTask(target, new TestLogger(), directory, "1 m");

        // When
        TaskExecutionResult fileResult = fileTask.execute();
        TaskExecutionResult dirResult = dirTask.execute();

        // Then
        assertThat(fileResult.status).isEqualTo(Success);
        assertThat(dirResult.status).isEqualTo(Success);
        assertThat(fileResult.outputs.get("type")).isEqualTo("regular file");
        assertThat(dirResult.outputs.get("type")).isEqualTo("directory");
        assertThat(fileResult.outputs).containsKeys(expectedKeys);
        assertThat(dirResult.outputs).containsKeys(expectedKeys);
    }

    @Test
    void should_upload_local_file_to_remote_destination() throws URISyntaxException, IOException {
        // Given
        Target target = FakeTargetInfo.buildTargetWithCredentialUsernamePassword(sftpServer);
        Path srcFilePath = Paths.get(SftpTaskTest.class.getResource("/sftptest.file.txt").toURI());
        String srcFile = srcFilePath.toString();

        String dstFile = temporaryFolder.resolve("bou.txt").toString();
        byte[] expectedContent = Files.newInputStream(srcFilePath).readAllBytes();
        Path expectedFile = temporaryFolder.resolve("bou.txt");

        SftpUploadTask task = new SftpUploadTask(target, mock(Logger.class), srcFile, dstFile, "5 s");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat(Files.exists(expectedFile)).isTrue();
        assertThat(expectedFile.toFile()).hasBinaryContent(expectedContent);

    }

    @Test
    void should_download_remote_file_to_local_destination() {
        // Given
        Target target = FakeTargetInfo.buildTargetWithCredentialUsernamePassword(sftpServer);
        String srcFile = SftpTaskTest.class.getResource("/sftptest.file.txt").getPath();
        String dstFile = temporaryFolder.resolve("downloaded").resolve("toto.txt").toString();
        Path expectedFile = temporaryFolder.resolve("downloaded").resolve("toto.txt");

        SftpDownloadTask task = new SftpDownloadTask(target, mock(Logger.class), srcFile, dstFile, "5 s");

        // When
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Success);
        assertThat(Files.exists(expectedFile)).isTrue();
    }
}

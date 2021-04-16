package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.ssh.fakes.FakeServerSsh.buildLocalServer;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildInfoWithPasswordFor;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildInfoWithPrivateKeyFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshj.Command;
import com.chutneytesting.task.ssh.sshj.CommandResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SshClientTaskTest {

    private static SshServer fakeSshServer;

    @BeforeAll
    public static void prepare_ssh_server() throws Exception {
        fakeSshServer = buildLocalServer();
        fakeSshServer.start();
    }

    @AfterAll
    public static void stop_ssh_server() throws Exception {
        fakeSshServer.stop();
    }

    @Test
    public void should_succeed_to_execute_a_command_with_password() {
        // Given
        Logger logger = mock(Logger.class);
        Target targetMock = buildInfoWithPasswordFor(fakeSshServer);
        List<CommandResult> expectedResults = new ArrayList<>();
        expectedResults.add(new CommandResult(new Command("echo Hello"), 0, "Hello\n", ""));

        List<Object> commands = Collections.singletonList("echo Hello");

        // When
        SshClientTask task = new SshClientTask(targetMock, logger, commands, null);
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
    }

    @Test
    public void should_succeed_to_execute_a_command_with_private_key() {
        // Given
        Logger logger = mock(Logger.class);
        Target target = buildInfoWithPrivateKeyFor(fakeSshServer);

        List<CommandResult> expectedResults = new ArrayList<>();
        expectedResults.add(new CommandResult(new Command("echo Hello"), 0, "Hello\n", ""));

        String command = "echo Hello";

        // When
        SshClientTask task = new SshClientTask(target, logger, Collections.singletonList(command), null);
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
    }

    @Test
    public void should_succeed_with_timed_out_result() {
        // Given
        Logger logger = mock(Logger.class);
        Target target = buildInfoWithPasswordFor(fakeSshServer);

        Map<String, String> command = new HashMap<>();
        command.put("command", OsUtils.isWin32() ? "START /WAIT TIMEOUT /T 1 /NOBREAK >NUL" : "sleep 1s");
        command.put("timeout", "1 ms");

        // When
        SshClientTask sshClient = new SshClientTask(target, logger, Collections.singletonList(command), null);
        TaskExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }

    @Test
    public void should_fail_when_target_server_is_not_responding() throws IOException {

        // Given
        Logger logger = mock(Logger.class);
        fakeSshServer.stop();
        Target target = buildInfoWithPasswordFor(fakeSshServer);
        String command = "echo Hello";

        // when
        SshClientTask sshClient = new SshClientTask(target, logger, Collections.singletonList(command), null);
        TaskExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }
}

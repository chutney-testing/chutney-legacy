package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.TaskExecutionResult.Status.Failure;
import static com.chutneytesting.task.ssh.fakes.FakeServerSsh.buildLocalServer;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildTargetWithCredentialUsernamePassword;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildTargetWithPrivateKeyWithCredentialPassphrase;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildTargetWithPrivateKeyWithPropertiesPassphrase;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildTargetWithPrivateKeyWithoutPassphrase;
import static com.chutneytesting.task.ssh.fakes.FakeTargetInfo.buildTargetWithPropertiesUsernamePassword;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshj.Command;
import com.chutneytesting.task.ssh.sshj.CommandResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.SshServer;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    @ParameterizedTest
    @MethodSource("usernamePasswordTargets")
    public void should_succeed_to_execute_a_command_with_password(Target targetMock) {
        // Given
        TestLogger logger = new TestLogger();
        List<CommandResult> expectedResults = new ArrayList<>();
        expectedResults.add(new CommandResult(new Command("echo Hello"), 0, "Hello\n", ""));

        List<Object> commands = singletonList("echo Hello");

        // When
        SshClientTask task = new SshClientTask(targetMock, logger, commands, null);
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
        assertThat(logger.info.get(0)).startsWith("Authentication via username/password as ");
    }

    public static List<Arguments> usernamePasswordTargets() {
        return List.of(
            Arguments.of(buildTargetWithCredentialUsernamePassword(fakeSshServer)),
            Arguments.of(buildTargetWithPropertiesUsernamePassword(fakeSshServer))
        );
    }

    @ParameterizedTest
    @MethodSource("usernamePrivateKeyTargets")
    public void should_succeed_to_execute_a_command_with_private_key(Target targetMock) {
        // Given
        TestLogger logger = new TestLogger();
        List<CommandResult> expectedResults = new ArrayList<>();
        expectedResults.add(new CommandResult(new Command("echo Hello"), 0, "Hello\n", ""));

        String command = "echo Hello";

        // When
        SshClientTask task = new SshClientTask(targetMock, logger, singletonList(command), null);
        TaskExecutionResult actualResult = task.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
        assertThat(logger.info.get(0)).startsWith("Authentication via private key as ");
    }

    public static List<Arguments> usernamePrivateKeyTargets() {
        return List.of(
            Arguments.of(buildTargetWithPrivateKeyWithoutPassphrase(fakeSshServer)),
            Arguments.of(buildTargetWithPrivateKeyWithCredentialPassphrase(fakeSshServer)),
            Arguments.of(buildTargetWithPrivateKeyWithPropertiesPassphrase(fakeSshServer))
        );
    }

    @Test
    public void should_succeed_with_timed_out_result() {
        // Given
        Logger logger = mock(Logger.class);
        Target target = buildTargetWithCredentialUsernamePassword(fakeSshServer);

        Map<String, String> command = new HashMap<>();
        command.put("command", OsUtils.isWin32() ? "START /WAIT TIMEOUT /T 1 /NOBREAK >NUL" : "sleep 1s");
        command.put("timeout", "1 ms");

        // When
        SshClientTask sshClient = new SshClientTask(target, logger, singletonList(command), null);
        TaskExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }

    @Test
    public void should_fail_when_target_server_is_not_responding() throws IOException {
        // Given
        Logger logger = mock(Logger.class);
        fakeSshServer.stop();
        Target target = buildTargetWithCredentialUsernamePassword(fakeSshServer);
        String command = "echo Hello";

        // when
        SshClientTask sshClient = new SshClientTask(target, logger, singletonList(command), null);
        TaskExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }

    @Test
    void should_validate_all_input() {
        SshClientTask sshClientTask = new SshClientTask(null, null, null, null);
        List<String> errors = sshClientTask.validateInputs();

        assertThat(errors.size()).isEqualTo(7);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors.get(0)).isEqualTo("No target provided");
        softly.assertThat(errors.get(1)).isEqualTo("[No target provided] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(2)).isEqualTo("[No url defined on the target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(3)).isEqualTo("[Target url is not valid] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(4)).isEqualTo("[Target url has an undefined host] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(5)).isEqualTo("No commands provided (List)");
        softly.assertThat(errors.get(6)).isEqualTo("[commands should not be empty] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertAll();
    }
}

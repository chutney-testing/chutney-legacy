package com.chutneytesting.task.ssh;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.TestFinallyActionRegistry;
import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import com.chutneytesting.task.ssh.sshj.CommandResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class SshServerTaskTest {

    private SshServerMock sshServer;

    @AfterEach
    public void tearDown() {
        if (sshServer != null) {
            SshServerStopTask sshServerStopTask = new SshServerStopTask(new TestLogger(), sshServer);
            sshServerStopTask.execute();

            await().atMost(500, MILLISECONDS).untilAsserted(() ->
                assertThat(sshServer.isClosed()).isTrue()
            );
        }
    }

    @Test
    void should_start_stop_server() {
        startSshServer(null, null, null);
    }

    @Test
    void should_refuse_wrong_user() {
        startSshServer(singletonList("user"), singletonList("pass"), null);

        Target targetMock = buildInfoWithPasswordFor(sshServer);
        SshClientTask task = new SshClientTask(targetMock, new TestLogger(), singletonList("echo Hello"), null);

        TaskExecutionResult actualResult = task.execute();

        assertThat(actualResult.status).isEqualTo(TaskExecutionResult.Status.Failure);
    }

    @Test
    void should_record_commands_sent_to_server() {
        startSshServer(null, null, null);

        Target targetMock = buildInfoWithPasswordFor(sshServer);
        List<Object> commands = Arrays.asList("echo Hello\r\nexit", "cat a_file\n\rexit");
        SshClientTask task = new SshClientTask(targetMock, new TestLogger(), commands, "shell");

        TaskExecutionResult actualResult = task.execute();

        assertThat(actualResult.status).isEqualTo(TaskExecutionResult.Status.Success);

        List<CommandResult> commandsResults = (List<CommandResult>) actualResult.outputs.get("results");
        assertThat(commandsResults.get(0).exitCode).isEqualTo(0);
        assertThat(commandsResults.get(1).exitCode).isEqualTo(0);

        assertThat(sshServer.commands()).containsExactlyElementsOf(commands.stream().map(Object::toString).collect(toList()));
        assertThat(sshServer.allStubsUsed()).isFalse();
    }

    @Test
    void should_send_stubs_matching_commands_sent_to_server() {
        List<String> stubs = Arrays.asList("Hello", "this is a file\ncontent...");
        startSshServer(null, null, stubs);

        Target targetMock = buildInfoWithPasswordFor(sshServer);
        List<Object> commands = Arrays.asList("echo Hello", "cat a_file");
        SshClientTask task = new SshClientTask(targetMock, new TestLogger(), commands, "command");

        TaskExecutionResult actualResult = task.execute();

        assertThat(actualResult.status).isEqualTo(TaskExecutionResult.Status.Success);

        List<CommandResult> commandsResults = (List<CommandResult>) actualResult.outputs.get("results");
        assertThat(commandsResults.get(0).stdout).isEqualTo(stubs.get(0));
        assertThat(commandsResults.get(0).exitCode).isEqualTo(0);
        assertThat(commandsResults.get(1).stdout).isEqualTo(stubs.get(1));
        assertThat(commandsResults.get(1).exitCode).isEqualTo(0);

        assertThat(sshServer.allStubsUsed()).isTrue();
    }

    private void startSshServer(List<String> usernames, List<String> passwords, List<String> stubs) {
        TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
        SshServerStartTask sshServerTask =
            new SshServerStartTask(
                new TestLogger(),
                finallyActionRegistry,
                null,
                null,
                null,
                usernames,
                passwords,
                "acceptAll",
                stubs
            );
        TaskExecutionResult result = sshServerTask.execute();
        sshServer = (SshServerMock) result.outputs.get("sshServer");

        assertThat(sshServer.isStarted()).isTrue();
        FinallyAction stopServerTask = finallyActionRegistry.finallyActions.get(0);
        assertThat(stopServerTask.actionIdentifier()).isEqualTo("ssh-server-stop");
        assertThat(stopServerTask.inputs().get("ssh-server")).isEqualTo(sshServer);
    }

    private Target buildInfoWithPasswordFor(SshServerMock sshServer) {
        SecurityInfo.Credential credential = mock(SecurityInfo.Credential.class);
        when(credential.username()).thenReturn("test");
        when(credential.password()).thenReturn("test");

        SecurityInfo securityInfoMock = mock(SecurityInfo.class);
        when(securityInfoMock.credential()).thenReturn(Optional.of(credential));

        return new HardcodedTarget(sshServer, securityInfoMock);
    }

    private static class HardcodedTarget implements Target {

        private final SshServerMock sshServer;
        private final SecurityInfo securityInfo;

        HardcodedTarget(SshServerMock sshServer, SecurityInfo securityInfoMock) {
            this.sshServer = sshServer;
            this.securityInfo = securityInfoMock;
        }

        @Override
        public String name() {
            return "SSH_SERVER";
        }

        @Override
        public String url() {
            return "ssh://" + sshServer.host() + ":" + sshServer.port();
        }

        @Override
        public Map<String, String> properties() {
            return Collections.emptyMap();
        }

        @Override
        public SecurityInfo security() {
            return securityInfo;
        }

    }
}

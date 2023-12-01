/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.ssh;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.action.TestFinallyActionRegistry;
import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.ssh.fakes.HardcodedTarget;
import com.chutneytesting.action.ssh.sshd.SshServerMock;
import com.chutneytesting.action.ssh.sshj.CommandResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SshServerActionTest {

    private SshServerMock sshServer;

    @AfterEach
    public void tearDown() throws InterruptedException {
        if (sshServer != null) {
            SshServerStopAction sshServerStopAction = new SshServerStopAction(new TestLogger(), sshServer);
            sshServerStopAction.execute();
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
        SshClientAction action = new SshClientAction(targetMock, new TestLogger(), singletonList("echo Hello"), null);

        ActionExecutionResult actualResult = action.execute();

        assertThat(actualResult.status).isEqualTo(ActionExecutionResult.Status.Failure);
    }

    @Test
    void should_record_commands_sent_to_server() {
        startSshServer(null, null, null);

        Target targetMock = buildInfoWithPasswordFor(sshServer);
        List<Object> commands = Arrays.asList("echo Hello\r\nexit", "cat a_file\n\rexit");
        SshClientAction action = new SshClientAction(targetMock, new TestLogger(), commands, "shell");

        ActionExecutionResult actualResult = action.execute();

        assertThat(actualResult.status).isEqualTo(ActionExecutionResult.Status.Success);

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
        SshClientAction action = new SshClientAction(targetMock, new TestLogger(), commands, "command");

        ActionExecutionResult actualResult = action.execute();

        assertThat(actualResult.status).isEqualTo(ActionExecutionResult.Status.Success);

        List<CommandResult> commandsResults = (List<CommandResult>) actualResult.outputs.get("results");
        assertThat(commandsResults.get(0).stdout).isEqualTo(stubs.get(0));
        assertThat(commandsResults.get(0).exitCode).isEqualTo(0);
        assertThat(commandsResults.get(1).stdout).isEqualTo(stubs.get(1));
        assertThat(commandsResults.get(1).exitCode).isEqualTo(0);

        assertThat(sshServer.allStubsUsed()).isTrue();
    }

    private void startSshServer(List<String> usernames, List<String> passwords, List<String> stubs) {
        TestFinallyActionRegistry finallyActionRegistry = new TestFinallyActionRegistry();
        SshServerStartAction sshServerAction =
            new SshServerStartAction(
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
        ActionExecutionResult result = sshServerAction.execute();
        sshServer = (SshServerMock) result.outputs.get("sshServer");

        await().untilAsserted(() -> {
            assertThat(sshServer.isStarted()).isTrue();
            assertThat(sshServer.isOpen()).isTrue();
        });
        FinallyAction stopServerAction = finallyActionRegistry.finallyActions.get(0);
        assertThat(stopServerAction.type()).isEqualTo("ssh-server-stop");
        assertThat(stopServerAction.inputs().get("ssh-server")).isEqualTo(sshServer);
    }

    private Target buildInfoWithPasswordFor(SshServerMock sshServer) {
        Map<String, String> properties = new HashMap<>();
        properties.put("username", "test");
        properties.put("password", "test");
        return new HardcodedTarget(sshServer, properties);
    }
}

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

import static com.chutneytesting.action.spi.ActionExecutionResult.Status.Failure;
import static com.chutneytesting.action.ssh.fakes.FakeServerSsh.buildLocalSshServer;
import static com.chutneytesting.action.ssh.fakes.FakeTargetInfo.buildTargetWithPassword;
import static com.chutneytesting.action.ssh.fakes.FakeTargetInfo.buildTargetWithPrivateKeyWithPassphrase;
import static com.chutneytesting.action.ssh.fakes.FakeTargetInfo.buildTargetWithPrivateKeyWithoutPassphrase;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.action.TestLogger;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.ssh.sshj.Command;
import com.chutneytesting.action.ssh.sshj.CommandResult;
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

public class SshClientActionTest {

    private static SshServer fakeSshServer;

    @BeforeAll
    public static void prepare_ssh_server() throws Exception {
        fakeSshServer = buildLocalSshServer();
        fakeSshServer.start();
    }

    @AfterAll
    public static void stop_ssh_server() throws Exception {
        fakeSshServer.stop();
    }

    @Test
    public void should_succeed_to_execute_a_command_with_password() {
        // Given
        Target targetMock = buildTargetWithPassword(fakeSshServer);
        TestLogger logger = new TestLogger();
        List<CommandResult> expectedResults = new ArrayList<>();
        expectedResults.add(new CommandResult(new Command("echo Hello"), 0, "Hello\n", ""));

        List<Object> commands = singletonList("echo Hello");

        // When
        SshClientAction action = new SshClientAction(targetMock, logger, commands, null);
        ActionExecutionResult actualResult = action.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
        assertThat(logger.info.get(0)).startsWith("Authentication via username/password as ");
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
        SshClientAction action = new SshClientAction(targetMock, logger, singletonList(command), null);
        ActionExecutionResult actualResult = action.execute();

        // Then
        assertThat(actualResult.outputs.get("results")).usingRecursiveComparison().isEqualTo(expectedResults);
        assertThat(logger.info.get(0)).startsWith("Authentication via private key as ");
    }

    public static List<Arguments> usernamePrivateKeyTargets() {
        return List.of(
            Arguments.of(buildTargetWithPrivateKeyWithoutPassphrase(fakeSshServer)),
            Arguments.of(buildTargetWithPrivateKeyWithPassphrase(fakeSshServer))
        );
    }

    @Test
    public void should_succeed_with_timed_out_result() {
        // Given
        Logger logger = mock(Logger.class);
        Target target = buildTargetWithPassword(fakeSshServer);

        Map<String, String> command = new HashMap<>();
        command.put("command", OsUtils.isWin32() ? "START /WAIT TIMEOUT /T 1 /NOBREAK >NUL" : "sleep 1s");
        command.put("timeout", "1 ms");

        // When
        SshClientAction sshClient = new SshClientAction(target, logger, singletonList(command), null);
        ActionExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }

    @Test
    public void should_fail_when_target_server_is_not_responding() throws IOException {
        // Given
        Logger logger = mock(Logger.class);
        fakeSshServer.stop();
        Target target = buildTargetWithPassword(fakeSshServer);
        String command = "echo Hello";

        // when
        SshClientAction sshClient = new SshClientAction(target, logger, singletonList(command), null);
        ActionExecutionResult actualResult = sshClient.execute();

        // Then
        assertThat(actualResult.status).isEqualTo(Failure);
    }

    @Test
    void should_validate_all_input() {
        SshClientAction sshClientAction = new SshClientAction(null, null, null, null);
        List<String> errors = sshClientAction.validateInputs();

        assertThat(errors.size()).isEqualTo(6);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(errors.get(0)).isEqualTo("No target provided");
        softly.assertThat(errors.get(1)).isEqualTo("[Target name is blank] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(2)).isEqualTo("[Target url is not valid: null target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(3)).isEqualTo("[Target url has an undefined host: null target] not applied because of exception java.lang.NullPointerException(null)");
        softly.assertThat(errors.get(4)).isEqualTo("No commands provided (List)");
        softly.assertThat(errors.get(5)).isEqualTo("[commands should not be empty] not applied because of exception java.lang.NullPointerException(Cannot invoke \"java.util.List.isEmpty()\" because \"m\" is null)");
        softly.assertAll();
    }
}

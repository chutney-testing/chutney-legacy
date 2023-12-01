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

package com.chutneytesting.agent.infra;

import static com.chutneytesting.agent.infra.HttpAgentClientTest.agentInfo;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class TcpConnectionCheckerTest {

    int connectionTimeout = 300;
    ConnectionChecker connectionChecker = new TcpConnectionChecker(connectionTimeout);

    @Test
    public void checker_fails_if_unknown_host() {
        assertThat(connectionChecker.canConnectTo(agentInfo("test", "missing_host", 3))).isFalse();

        // Timeout here depends on DNS configure
    }

    @Test
    public void checker_fails_if_port_is_not_listened() {
        Stopwatch sw = Stopwatch.createStarted();
        assertThat(connectionChecker.canConnectTo(agentInfo("test", Localhost.ip(), 3))).isFalse();
        sw.stop();

        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isLessThan(connectionTimeout + 1000);
    }

    @Test
    public void checker_succeed_if_port_is_listened() throws IOException {
        Stopwatch sw = Stopwatch.createUnstarted();
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            sw.start();
            assertThat(connectionChecker.canConnectTo(agentInfo("test", Localhost.ip(), port))).isTrue();
            sw.stop();
        }

        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isLessThan(connectionTimeout);
    }
}

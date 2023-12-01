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

import static com.chutneytesting.ServerConfigurationValues.AGENT_NETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE;

import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TcpConnectionChecker implements ConnectionChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpConnectionChecker.class);
    private final int timeout;

    TcpConnectionChecker(@Value(AGENT_NETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE) int timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean canConnectTo(NamedHostAndPort namedHostAndPort) {
        boolean reached = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(namedHostAndPort.host(), namedHostAndPort.port()), timeout);
            reached = true;
        } catch (IOException e) {
            LOGGER.warn("Unable to connect to {} ({}: {})", namedHostAndPort, e.getClass().getSimpleName(), e.getMessage());
        }
        return reached;
    }
}

package com.chutneytesting.agent.infra;

import static com.chutneytesting.ServerConfiguration.AGENTNETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE;

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

    TcpConnectionChecker(@Value(AGENTNETWORK_CONNECTION_CHECK_TIMEOUT_SPRING_VALUE) int timeout) {
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

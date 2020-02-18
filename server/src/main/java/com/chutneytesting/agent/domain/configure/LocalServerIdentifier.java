package com.chutneytesting.agent.domain.configure;

import static java.util.Collections.list;

import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.stream.Stream;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.ThrowingPredicate;

public class LocalServerIdentifier {
    private final int port;
    private final String defaultLocalName;
    private final String defaultLocalHostName;

    public LocalServerIdentifier(int port, String defaultLocalName, String defaultLocalHostName) {
        this.port = port;
        this.defaultLocalName = defaultLocalName;
        this.defaultLocalHostName = defaultLocalHostName;
    }

    public String getLocalName(NetworkConfiguration networkConfiguration) {
        return findLocalName(networkConfiguration).orElse(defaultLocalName);
    }

    private Optional<String> findLocalName(NetworkConfiguration networkConfiguration) {
        return networkConfiguration.agentNetworkConfiguration().agentInfos().stream()
            .filter(this::isLocalInstance)
            .findFirst()
            .map(NamedHostAndPort::name);
    }

    public Agent findLocalAgent(AgentGraph agentGraph) {
        return agentGraph.agents().stream()
            .filter(agent -> isLocalInstance(agent.agentInfo))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Impossible to find local agent in the given graph. (0_o) ! It seems that your machine doesn't know itself. " +
                "Check your /etc/hosts file or equivalent. Hostname should match actual IP."
            ));
    }

    private boolean isLocalInstance(NamedHostAndPort namedHostAndPort) {
        return port == namedHostAndPort.port() && isLocalHost(namedHostAndPort);
    }

    private boolean isLocalHost(NamedHostAndPort namedHostAndPort) {
        try {
            return list(NetworkInterface.getNetworkInterfaces())
                .stream()
                .filter(ThrowingPredicate.toUnchecked(NetworkInterface::isUp))
                .map(ThrowingFunction.toUnchecked(NetworkInterface::getInetAddresses))
                .flatMap(addresses -> list(addresses).stream())
                .flatMap(address -> Stream.of(address.getCanonicalHostName(), address.getHostAddress()))
                .anyMatch(host -> host.equalsIgnoreCase(namedHostAndPort.host()))
                ||
                InetAddress.getLocalHost().getCanonicalHostName().equalsIgnoreCase(namedHostAndPort.host());
        } catch (SocketException | IllegalStateException | UnknownHostException e) {
            return false;
        }
    }

    public NetworkConfiguration withLocalHost(NetworkConfiguration networkConfiguration) {
        if (!findLocalName(networkConfiguration).isPresent())
            return ImmutableNetworkConfiguration.builder()
                .from(networkConfiguration)
                .agentNetworkConfiguration(ImmutableNetworkConfiguration.AgentNetworkConfiguration.builder()
                    .from(networkConfiguration.agentNetworkConfiguration())
                    .addAgentInfos(new NamedHostAndPort(defaultLocalName, defaultLocalHostName, port))
                    .build())
                .build();
        return networkConfiguration;
    }
}

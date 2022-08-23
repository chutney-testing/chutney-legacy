package com.chutneytesting.agent.domain.configure;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.ThrowingPredicate;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalServerIdentifier {
    private final int port;
    private final String defaultLocalName;
    private final String defaultLocalHostName;
    private List<String> localHostsAndIps;

    public LocalServerIdentifier(int port, String defaultLocalName, String defaultLocalHostName) {
        this.port = port;
        this.defaultLocalName = defaultLocalName;
        this.defaultLocalHostName = defaultLocalHostName;

        try {
            this.localHostsAndIps = list(NetworkInterface.getNetworkInterfaces())
                .stream()
                .filter(ThrowingPredicate.toUnchecked(NetworkInterface::isUp))
                .map(ThrowingFunction.toUnchecked(NetworkInterface::getInetAddresses))
                .flatMap(addresses -> list(addresses).stream())
                .flatMap(address -> Stream.of(address.getCanonicalHostName(), address.getHostAddress()))
                .collect(Collectors.toList());
        } catch (SocketException e) {
            this.localHostsAndIps = emptyList();
        }
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
            return localHostsAndIps.stream()
                .anyMatch(host -> host.equalsIgnoreCase(namedHostAndPort.host()))
                ||
                InetAddress.getLocalHost().getCanonicalHostName().equalsIgnoreCase(namedHostAndPort.host());
        } catch (IllegalStateException | UnknownHostException e) {
            return false;
        }
    }

    public NetworkConfiguration withLocalHost(NetworkConfiguration networkConfiguration) {
        if (findLocalName(networkConfiguration).isEmpty())
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

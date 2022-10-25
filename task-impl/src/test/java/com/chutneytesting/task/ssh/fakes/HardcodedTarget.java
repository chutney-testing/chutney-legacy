package com.chutneytesting.task.ssh.fakes;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.sshd.server.SshServer;

public class HardcodedTarget implements Target {

    private final String host;
    private final int port;
    private final Map<String, String> properties;

    public HardcodedTarget(SshServerMock sshServer, Map<String, String> properties) {
        this.host = sshServer.host();
        this.port = sshServer.port();
        this.properties = unmodifiableMap(properties);
    }

    public HardcodedTarget(SshServer sshServer, Map<String, String> properties) {
        this.host = sshServer.getHost();
        this.port = sshServer.getPort();
        this.properties = unmodifiableMap(properties);
    }

    @Override
    public String name() {
        return "SSH_SERVER";
    }

    @Override
    public URI uri() {
        try {
            return new URI("ssh://" + host + ":" + port);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String rawUri() {
        return uri().toString();
    }

    @Override
    public Optional<String> property(String key) {
        return ofNullable(properties.get(key));
    }

    @Override
    public Map<String, String> prefixedProperties(String prefix, boolean cutPrefix) {
        return properties.entrySet().stream()
            .filter(e -> e.getKey() != null)
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(toMap(e -> e.getKey().substring(cutPrefix ? prefix.length() : 0), Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return "HardcodedTarget{" +
            ", host=" + host +
            ", port=" + port +
            ", properties=" + properties +
            '}';
    }
}

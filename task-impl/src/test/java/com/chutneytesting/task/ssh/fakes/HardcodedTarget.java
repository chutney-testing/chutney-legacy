package com.chutneytesting.task.ssh.fakes;

import static java.util.Collections.unmodifiableMap;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
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
    public String url() {
        return "ssh://" + host + ":" + port;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public SecurityInfo security() {
        return null;
    }

    @Override
    public URI uri() {
        try {
            return new URI(url());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
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

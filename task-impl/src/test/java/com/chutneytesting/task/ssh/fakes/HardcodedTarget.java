package com.chutneytesting.task.ssh.fakes;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import java.util.Map;
import org.apache.sshd.server.SshServer;

public class HardcodedTarget implements Target {

    private final String host;
    private final int port;
    private final SecurityInfo securityInfo;
    private final Map<String, String> properties;

    public HardcodedTarget(SshServerMock sshServer, SecurityInfo securityInfo) {
        this.host = sshServer.host();
        this.port = sshServer.port();
        this.securityInfo = securityInfo;
        this.properties = emptyMap();
    }

    public HardcodedTarget(SshServer sshServer, SecurityInfo securityInfo, Map<String, String> properties) {
        this.host = sshServer.getHost();
        this.port = sshServer.getPort();
        this.securityInfo = securityInfo;
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
        return securityInfo;
    }

    @Override
    public String toString() {
        return "HardcodedTarget{" +
            "securityInfo=" + securityInfo +
            ", properties=" + properties +
            '}';
    }
}

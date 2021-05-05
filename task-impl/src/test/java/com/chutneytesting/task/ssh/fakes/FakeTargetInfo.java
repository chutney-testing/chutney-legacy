package com.chutneytesting.task.ssh.fakes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.sshd.server.SshServer;

public class FakeTargetInfo {

    private static final String RSA_PRIVATE_KEY = FakeTargetInfo.class.getResource("/security/client.key").getPath();

    public static Target buildInfoWithPasswordFor(SshServer sshServer) {
        SecurityInfo.Credential credential = mock(SecurityInfo.Credential.class);
        when(credential.username()).thenReturn(FakeServerSsh.USERNAME);
        when(credential.password()).thenReturn(FakeServerSsh.PASSWORD);

        SecurityInfo securityInfoMock = mock(SecurityInfo.class);
        when(securityInfoMock.credential()).thenReturn(Optional.of(credential));

        return new HardcodedTarget(sshServer, securityInfoMock);
    }

    public static Target buildInfoWithPrivateKeyFor(SshServer sshServer) {

        SecurityInfo.Credential credential = mock(SecurityInfo.Credential.class);
        when(credential.username()).thenReturn(FakeServerSsh.USERNAME);
        when(credential.password()).thenReturn("");

        SecurityInfo securityInfoMock = mock(SecurityInfo.class);
        when(securityInfoMock.credential()).thenReturn(Optional.of(credential));
        when(securityInfoMock.privateKey()).thenReturn(Optional.of(RSA_PRIVATE_KEY));

        return new HardcodedTarget(sshServer, securityInfoMock);
    }

    private static class HardcodedTarget implements Target {

        private final SshServer sshServer;
        private final SecurityInfo securityInfo;

        HardcodedTarget(SshServer sshServer, SecurityInfo securityInfo) {
            this.sshServer = sshServer;
            this.securityInfo = securityInfo;
        }

        @Override
        public String name() {
            return "SSH_SERVER";
        }

        @Override
        public String url() {
            return "ssh://" + sshServer.getHost() + ":" + sshServer.getPort();
        }

        @Override
        public Map<String, String> properties() {
            return Collections.emptyMap();
        }

        @Override
        public SecurityInfo security() {
            return securityInfo;
        }

    }
}

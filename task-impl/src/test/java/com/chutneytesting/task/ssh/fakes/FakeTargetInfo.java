package com.chutneytesting.task.ssh.fakes;

import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
import java.nio.file.Path;
import java.util.Map;
import org.apache.sshd.server.SshServer;

public class FakeTargetInfo {

    private static final String RSA_PRIVATE_KEY = FakeTargetInfo.class.getResource("/security/client_rsa.key").getPath();
    private static final String ECDSA_PRIVATE_KEY = FakeTargetInfo.class.getResource("/security/client_ecdsa.key").getPath();

    public static Target buildTargetWithCredentialUsernamePassword(SshServer sshServer) {
        return buildTarget(sshServer, FakeServerSsh.USERNAME, FakeServerSsh.PASSWORD, null, emptyMap());
    }

    public static Target buildTargetWithPropertiesUsernamePassword(SshServer sshServer) {
        return buildTarget(sshServer, null, null, null,
            Map.of("username", FakeServerSsh.USERNAME, "password", FakeServerSsh.PASSWORD)
        );
    }

    public static Target buildTargetWithPrivateKeyWithoutPassphrase(SshServer sshServer) {
        return buildTarget(sshServer, FakeServerSsh.USERNAME, "", RSA_PRIVATE_KEY, emptyMap());
    }

    public static Target buildTargetWithPrivateKeyWithCredentialPassphrase(SshServer sshServer) {
        return buildTarget(sshServer, FakeServerSsh.USERNAME, "password", ECDSA_PRIVATE_KEY, emptyMap());
    }

    public static Target buildTargetWithPrivateKeyWithPropertiesPassphrase(SshServer sshServer) {
        return buildTarget(sshServer, FakeServerSsh.USERNAME, null, ECDSA_PRIVATE_KEY,
            Map.of("privateKeyPassphrase", "password")
        );
    }

    public static Target buildTarget(SshServer sshServer, String credentialUserName, String credentialPassword, String privateKeyPath, Map<String, String> properties) {
        SecurityInfo.Credential credential = mock(SecurityInfo.Credential.class);
        ofNullable(credentialUserName).ifPresent(cun -> when(credential.username()).thenReturn(cun));
        ofNullable(credentialPassword).ifPresent(cp -> when(credential.password()).thenReturn(cp));

        SecurityInfo securityInfoMock = mock(SecurityInfo.class);
        when(securityInfoMock.credential()).thenReturn(of(credential));
        ofNullable(privateKeyPath).ifPresent(pkp -> when(securityInfoMock.privateKey()).thenReturn(of(pkp)));

        when(securityInfoMock.toString()).thenReturn(
            "{username = '" + credentialUserName +
                "', password='" + credentialPassword +
                "',privateKey=" + ofNullable(privateKeyPath).map(pk -> pk.substring(1)).map(pk -> Path.of(pk).getFileName()).orElse(null) +
                "}"
        );

        return new HardcodedTarget(sshServer, securityInfoMock, properties);
    }
}

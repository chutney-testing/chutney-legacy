package com.chutneytesting.task.ssh.fakes;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import org.apache.sshd.server.SshServer;

public class FakeTargetInfo {

    private static final String RSA_PRIVATE_KEY = FakeTargetInfo.class.getResource("/security/client_rsa.key").getPath();
    private static final String ECDSA_PRIVATE_KEY = FakeTargetInfo.class.getResource("/security/client_ecdsa.key").getPath();

    public static Target buildTargetWithPassword(SshServer sshServer) {
        return buildTarget(sshServer, FakeServerSsh.PASSWORD, null, null);
    }

    public static Target buildTargetWithPrivateKeyWithoutPassphrase(SshServer sshServer) {
        return buildTarget(sshServer, null, RSA_PRIVATE_KEY, null);
    }

    public static Target buildTargetWithPrivateKeyWithPassphrase(SshServer sshServer) {
        return buildTarget(sshServer, null, ECDSA_PRIVATE_KEY, "password");
    }

    private static Target buildTarget(SshServer sshServer, String userPassword, String privateKeyPath, String privateKeyPassphrase) {
        Map<String, String> properties = new HashMap<>();
        properties.put("user", FakeServerSsh.USERNAME);
        ofNullable(userPassword).ifPresent(cp -> properties.put("password", cp));
        ofNullable(privateKeyPath).ifPresent(pkp -> properties.put("privateKey", pkp));
        ofNullable(privateKeyPassphrase).ifPresent(pkp -> properties.put("privateKeyPassphrase", pkp));
        return new HardcodedTarget(sshServer, properties);
    }
}

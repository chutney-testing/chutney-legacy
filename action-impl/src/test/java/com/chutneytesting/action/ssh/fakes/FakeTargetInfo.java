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

package com.chutneytesting.action.ssh.fakes;

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.Target;
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

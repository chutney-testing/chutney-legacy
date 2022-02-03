package com.chutneytesting.task.ssh;

import static java.util.Collections.singletonList;

import com.chutneytesting.task.spi.injectable.Target;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;

public class SshClientFactory {

    public static String DEFAULT_TIMEOUT = "5 s";

    public static ClientSession buildSSHClientSession(Target target, long timeout) throws IOException {
        Connection connection = Connection.from(target);
        SshClient defaultClient = createDefaultClient();
        defaultClient.setUserAuthFactories(getAuthFactory(connection));
        ClientSession session = getConnectedSession(defaultClient, connection);

        session.auth().verify(timeout);
        return session;
    }

    private static SshClient createDefaultClient() {
        SshClient defaultClient = SshClient.setUpDefaultClient();
        defaultClient.start();
        return defaultClient;
    }

    private static List<UserAuthFactory> getAuthFactory(Connection connection) {
        if (connection.usePrivateKey()) {
            return singletonList(UserAuthPublicKeyFactory.INSTANCE);
        }
        return singletonList(UserAuthPasswordFactory.INSTANCE);
    }

    private static ClientSession getConnectedSession(SshClient client, Connection connection) throws IOException {
        ConnectFuture connectFuture = client.connect(connection.username, connection.serverHost, connection.serverPort).verify();
        return configureSessionAuthMethod(connectFuture.getSession(), connection);
    }

    private static ClientSession configureSessionAuthMethod(ClientSession session, Connection connection) {
        if (connection.usePrivateKey()) {
            FileKeyPairProvider provider = new FileKeyPairProvider(new File(connection.privateKey).toPath());
            provider.setPasswordFinder(FilePasswordProvider.of(connection.passphrase));
            session.setKeyIdentityProvider(provider);
        } else {
            session.addPasswordIdentity(connection.password);
        }
        return session;
    }

}

package com.chutneytesting.task.ssh.scp;

import static java.util.Collections.singletonList;

import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshj.Connection;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.scp.client.CloseableScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;

public class ScpClientImpl implements ScpClient {

    private final ClientSession session;
    private final CloseableScpClient sshClient;

    private ScpClientImpl(ClientSession session, CloseableScpClient sshClient) {
        this.session = session;
        this.sshClient = sshClient;
    }

    @Override
    public void upload(String local, String remote) throws IOException {
        sshClient.upload(local, remote, Collections.emptyList());
    }

    @Override
    public void download(String remote, String local) throws IOException {
        sshClient.download(remote, local, Collections.emptyList());
    }

    @Override
    public void close() throws Exception {
        session.close();
        sshClient.close();
    }

    public static ScpClient buildFor(Target target, long timeout) throws IOException {
        Connection connection = Connection.from(target);
        SshClient defaultClient = createDefaultClient();
        defaultClient.setUserAuthFactories(getAuthFactory(connection));
        ClientSession session = getConnectedSession(defaultClient, connection);

        session.auth().verify(timeout);

        return new ScpClientImpl(session, closeableClient(session));
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

    private static CloseableScpClient closeableClient(ClientSession session) {
        ScpClientCreator creator = ScpClientCreator.instance();
        return CloseableScpClient.singleSessionInstance(
            creator.createScpClient(session)
        );
    }

}

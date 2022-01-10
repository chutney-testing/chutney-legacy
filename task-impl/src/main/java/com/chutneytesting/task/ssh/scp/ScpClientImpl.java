package com.chutneytesting.task.ssh.scp;

import com.chutneytesting.task.ssh.sshj.Connection;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.future.AuthFuture;
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
    public void close() throws IOException {
        session.close();
        sshClient.close();
    }

    public static ScpClientBuilder builder() {
        return new ScpClientBuilder();
    }

    public static class ScpClientBuilder {
        private Connection connection;

        public ScpClientBuilder withConnection(Connection connection) {
            this.connection = connection;
            return this;
        }

        private SshClient createScpClient() {
            SshClient defaultClient = SshClient.setUpDefaultClient();
            defaultClient.start();
            return defaultClient;
        }

        public ScpClient build() throws IOException {
            SshClient defaultClient = createScpClient();
            defaultClient.setUserAuthFactories(getAuthFactory());
            ClientSession session = connect(defaultClient);

            AuthFuture authFuture = session.auth().verify();

            ScpClientCreator creator = ScpClientCreator.instance();
            CloseableScpClient scpClient = CloseableScpClient.singleSessionInstance(
                creator.createScpClient(session)
            );
            return new ScpClientImpl(session, scpClient);
        }

        private List<UserAuthFactory> getAuthFactory() {
            if (connection.usePrivateKey()) {
                return Collections.singletonList(UserAuthPublicKeyFactory.INSTANCE);
            }
            return Collections.singletonList(UserAuthPasswordFactory.INSTANCE);
        }

        private ClientSession connect(SshClient client) throws IOException {
            ConnectFuture connectFuture = client.connect(connection.username, connection.serverHost, connection.serverPort).verify();
            return configureAuthMethod(connectFuture.getSession());
        }

        private ClientSession configureAuthMethod(ClientSession session) {
            if (connection.usePrivateKey()) {
                FileKeyPairProvider provider = new FileKeyPairProvider(Path.of(connection.privateKey));
                provider.setPasswordFinder(FilePasswordProvider.of(connection.passphrase));
                session.setKeyIdentityProvider(provider);
            } else {
                session.addPasswordIdentity(connection.password);
            }
            return session;
        }

    }

    /*
    protected static final ScpTransferEventListener DEBUG_LISTENER = new ScpTransferEventListener() {
        @Override
        public void startFolderEvent(
            Session s, FileOperation op, Path file, Set<PosixFilePermission> perms) {
            logEvent("starFolderEvent", s, op, file, false, -1L, perms, null);
        }

        @Override
        public void startFileEvent(
            Session s, FileOperation op, Path file, long length, Set<PosixFilePermission> perms) {
            logEvent("startFileEvent", s, op, file, true, length, perms, null);
        }

        @Override
        public void endFolderEvent(
            Session s, FileOperation op, Path file, Set<PosixFilePermission> perms, Throwable thrown) {
            logEvent("endFolderEvent", s, op, file, false, -1L, perms, thrown);
        }

        @Override
        public void endFileEvent(
            Session s, FileOperation op, Path file, long length, Set<PosixFilePermission> perms, Throwable thrown) {
            logEvent("endFileEvent", s, op, file, true, length, perms, thrown);
        }

        @Override
        public void handleFileEventAckInfo(
            Session session, FileOperation op, Path file, long length,
            Set<PosixFilePermission> perms, ScpAckInfo ackInfo)
            throws IOException {
            logEvent("ackInfo(" + ackInfo + ")", session, op, file, true, length, perms, null);
        }

        private void logEvent(
            String type, Session s, FileOperation op, Path path, boolean isFile,
            long length, Collection<PosixFilePermission> perms, Throwable t) {
            if (!OUTPUT_DEBUG_MESSAGES) {
                return; // just in case
            }
            StringBuilder sb = new StringBuilder(Byte.MAX_VALUE);
            sb.append("    ").append(type)
                .append('[').append(s).append(']')
                .append('[').append(op).append(']')
                .append(' ').append(isFile ? "File" : "Directory").append('=').append(path)
                .append(' ').append("length=").append(length)
                .append(' ').append("perms=").append(perms);
            if (t != null) {
                sb.append(' ').append("ERROR=").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
            }
            outputDebugMessage(sb.toString());
        }
    };
*/

/*    public static void outputDebugMessage(Object message) {
        if (OUTPUT_DEBUG_MESSAGES) {
            System.out.append("===[DEBUG]=== ").println(message);
        }
    }*/
}

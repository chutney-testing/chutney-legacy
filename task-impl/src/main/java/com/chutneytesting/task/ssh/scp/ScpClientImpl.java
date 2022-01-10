package com.chutneytesting.task.ssh.scp;

import com.chutneytesting.task.ssh.sshj.Connection;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.UserAuthFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.scp.client.CloseableScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.junit.platform.commons.util.StringUtils;

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
        //        private Connection connection;

        private String username;
        private String host;
        private int port;

        private String password;
        private String privateKey;
        private String passphrase;

        private List<UserAuthFactory> userAuthFactory;

        public ScpClientBuilder withConnection(Connection connection) {
//            this.connection = connection;
            return withHost(connection.serverHost)
                .withPort(connection.serverPort)
                .withUsername(connection.username)
                .withPassword(connection.password)
                .withPrivateKey(connection.privateKey)
                .withPassphrase(connection.passphrase);
        }

        public ScpClientBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public ScpClientBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public ScpClientBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public ScpClientBuilder withPassword(String password) {
            this.password = password;
            this.setUserAuthFactory(Collections.singletonList(UserAuthPasswordFactory.INSTANCE));
            return this;
        }

        public ScpClientBuilder withPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            if (usePrivateKey()) {
                this.setUserAuthFactory(Collections.singletonList(UserAuthPublicKeyFactory.INSTANCE));
            }
            return this;
        }

        public ScpClientBuilder withPassphrase(String passphrase) {
            this.passphrase = passphrase;
            return this;
        }


        private void setUserAuthFactory(List<UserAuthFactory> userAuthFactory) {
            this.userAuthFactory = userAuthFactory;
        }

        private boolean usePrivateKey() {
            return StringUtils.isNotBlank(privateKey);
        }

        private SshClient createScpClient() {
            SshClient defaultClient = SshClient.setUpDefaultClient();
            defaultClient.start();
            return defaultClient;
        }

        public ScpClient build() throws IOException {
            SshClient defaultClient = createScpClient();
            ClientSession session;

            defaultClient.setUserAuthFactories(userAuthFactory);
            session = connect(defaultClient);
            if (usePrivateKey()) {
                FileKeyPairProvider provider = new FileKeyPairProvider(Path.of(privateKey));
                provider.setPasswordFinder(FilePasswordProvider.of(passphrase));
                session.setKeyIdentityProvider(provider);
            } else {
                session.addPasswordIdentity(password);
            }

            AuthFuture authFuture = session.auth().verify();

            ScpClientCreator creator = ScpClientCreator.instance();
            CloseableScpClient scpClient = CloseableScpClient.singleSessionInstance(
                creator.createScpClient(session)
            );
            return new ScpClientImpl(session, scpClient);
        }

        private ClientSession connect(SshClient client) throws IOException {
            ConnectFuture connectFuture = client.connect(username, host, port).verify();
            return connectFuture.getSession();
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

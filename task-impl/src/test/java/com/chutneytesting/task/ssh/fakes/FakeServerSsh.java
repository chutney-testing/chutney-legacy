package com.chutneytesting.task.ssh.fakes;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.RejectAllPasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.junit.platform.commons.util.StringUtils;

public class FakeServerSsh {

    static final String USERNAME = "mockssh";
    static final String PASSWORD = "mockssh";

    private static final String DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM = "RSA";

    public static SshServer buildLocalServer() throws IOException {
        return buildLocalServer(true, true, 20, "");
    }

    public static SshServer buildLocalServer(boolean acceptAllPubKeys, boolean acceptAllPassword, int maxAuthRequests) throws IOException {
        return buildLocalServer(acceptAllPubKeys, acceptAllPassword, maxAuthRequests, "");
    }

    public static SshServer buildLocalServer(boolean acceptAllPubKeys, boolean acceptAllPassword, int maxAuthRequests, String authorizedKeys) throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setHost(getHostIPAddress());
        sshd.setPort(getFreePort());
        AbstractGeneratorHostKeyProvider hostKeyProvider = prepareKeyPairProvider();
        sshd.setKeyPairProvider(hostKeyProvider);
        sshd.setPublickeyAuthenticator(getPublickeyAuthenticator(acceptAllPubKeys, authorizedKeys));
        sshd.setPasswordAuthenticator(acceptAllPassword ? (username, password, session) -> USERNAME.equals(username) && PASSWORD.equals(password) : RejectAllPasswordAuthenticator.INSTANCE);
        sshd.setShellFactory(InteractiveProcessShellFactory.INSTANCE);
        sshd.setCommandFactory(ProcessShellCommandFactory.INSTANCE);
        CoreModuleProperties.MAX_AUTH_REQUESTS.set(sshd, maxAuthRequests);
        return sshd;
    }

    private static PublickeyAuthenticator getPublickeyAuthenticator(boolean acceptAllPubKeys, String authorizedKeys) {
        if (StringUtils.isNotBlank(authorizedKeys)) {
            return new AuthorizedKeysAuthenticator(Path.of(authorizedKeys));
        }
        return acceptAllPubKeys ? AcceptAllPublickeyAuthenticator.INSTANCE : RejectAllPublickeyAuthenticator.INSTANCE;
    }

    private static AbstractGeneratorHostKeyProvider prepareKeyPairProvider() {

        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider();

        hostKeyProvider.setAlgorithm(DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM);
        File rsaKey;
        try {
            rsaKey = File.createTempFile("test", "ssh_key_pair");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        hostKeyProvider.setPath(rsaKey.toPath());
        return hostKeyProvider;
    }

    private static String getHostIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private static int getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

}

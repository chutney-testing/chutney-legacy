package com.chutneytesting.task.ssh.fakes;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

public class FakeServerSsh {

    static final String USERNAME = "mockssh";
    static final String PASSWORD = "mockssh";

    private static final String DEFAULT_TEST_HOST_KEY_PROVIDER_ALGORITHM = "RSA";

    public static SshServer buildLocalServer() throws IOException, InterruptedException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setHost(getHostIPAddress());
        sshd.setPort(getFreePort());
        AbstractGeneratorHostKeyProvider hostKeyProvider = prepareKeyPairProvider();
        sshd.setKeyPairProvider(hostKeyProvider);
        sshd.setPasswordAuthenticator((username, password, session) -> USERNAME.equals(username) && PASSWORD.equals(password));
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
        sshd.setShellFactory(new ProcessShellFactory("/bin/sh", "-i", "-l"));
        sshd.setCommandFactory(command -> new ProcessShellFactory(command.split(" ")).create());
        return sshd;
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

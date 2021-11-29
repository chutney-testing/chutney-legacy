package com.chutneytesting.task.ssh;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.ssh.sshd.ChutneyCommandFactory;
import com.chutneytesting.task.ssh.sshd.NoShellFactory;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.LoggerFactory;

public class SshServerStartTask implements Task {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SshServerStartTask.class);

    private final Logger logger;
    private final FinallyActionRegistry finallyActionRegistry;
    private final int port;
    private final String host;
    private final String keyPair;
    private final List<String> sshUsernames;
    private final List<String> sshPasswords;
    private final String authorizedKeys;
    private final List<String> stubs;

    public SshServerStartTask(Logger logger,
                              FinallyActionRegistry finallyActionRegistry,
                              @Input("port") String port,
                              @Input("bind-address") String host,
                              @Input("private-key") String keyPair,
                              @Input("usernames") List<String> usernames,
                              @Input("passwords") List<String> passwords,
                              @Input("authorized-keys") String authorizedKeys,
                              @Input("responses") List<String> stubs) {
        this.logger = logger;
        this.finallyActionRegistry = finallyActionRegistry;
        this.port = Integer.parseInt(ofNullable(port).orElseGet(() -> String.valueOf(getFreePort())));
        this.host = ofNullable(host).orElseGet(this::getHostAddress);
        this.keyPair = keyPair;
        this.sshUsernames = ofNullable(usernames).filter(l -> !l.isEmpty()).orElse(singletonList("test"));
        this.sshPasswords = ofNullable(passwords).filter(l -> !l.isEmpty()).orElse(singletonList("test"));
        this.authorizedKeys = ofNullable(authorizedKeys).orElse("default");
        this.stubs = ofNullable(stubs).orElse(emptyList());
    }

    @Override
    public TaskExecutionResult execute() {
        SshServer sshServer = SshServer.setUpDefaultServer();
        SshServerMock mock = new SshServerMock(sshServer, stubs);

        sshServer.setPort(port);
        sshServer.setHost(host);
        sshServer.setKeyPairProvider(keyPairProvider());
        //sshServer.setHostKeyCertificateProvider();
        sshServer.setShellFactory(new NoShellFactory(mock));
        sshServer.setCommandFactory(new ChutneyCommandFactory(mock));

        sshServer.setPasswordAuthenticator(simplePasswordAuthenticator());
        sshServer.setPublickeyAuthenticator(publicKeyAuthenticator());

        logger.info("Try to start ssh server on port " + port);
        try {
            mock.start();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        createQuitFinallyAction(mock);
        return TaskExecutionResult.ok(toOutputs(mock));
    }

    private Map<String, Object> toOutputs(SshServerMock sshServer) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("sshServer", sshServer);
        return outputs;
    }

    private void createQuitFinallyAction(SshServerMock sshServer) {
        finallyActionRegistry.registerFinallyAction(
            FinallyAction.Builder
                .forAction("ssh-server-stop", SshServerStartTask.class)
                .withInput("ssh-server", sshServer)
                .build()
        );
        logger.info("SshServerStop finally action registered");
    }

    private KeyPairProvider keyPairProvider() {
        if (keyPair != null && !keyPair.isEmpty()) {
            Path keyPairPath = Paths.get(keyPair);
            if (Files.exists(keyPairPath)) {
                return new FileKeyPairProvider(keyPairPath);
            } else {
                return new ClassLoadableResourceKeyPairProvider(keyPair);
            }
        }
        return simpleKeyPairProvider();
    }

    private AbstractGeneratorHostKeyProvider simpleKeyPairProvider() {
        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider();
        hostKeyProvider.setAlgorithm("RSA");
        File rsaKey;
        try {
            rsaKey = File.createTempFile("chutney", "ssh_key_pair");
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        hostKeyProvider.setPath(rsaKey.toPath());
        return hostKeyProvider;
    }

    private PublickeyAuthenticator publicKeyAuthenticator() {
        if ("default".equals(authorizedKeys)) {
            return DefaultAuthorizedKeysAuthenticator.INSTANCE;
        } else if ("rejectAll".equals(authorizedKeys)) {
            return RejectAllPublickeyAuthenticator.INSTANCE;
        } else if ("acceptAll".equals(authorizedKeys)) {
            return AcceptAllPublickeyAuthenticator.INSTANCE;
        } else {
            Path path = Paths.get(authorizedKeys);
            if (Files.exists(path)) {
                return new AuthorizedKeysAuthenticator(path);
            } else {
                throw new UncheckedIOException(new FileNotFoundException(path.toString()));
            }
        }
    }

    private int getFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warn("Cannot retrieve host IP address", e);
            return "0.0.0.0";
        }
    }

    private PasswordAuthenticator simplePasswordAuthenticator() {
        return (username, password, session) ->
            sshUsernames.contains(username) && sshPasswords.contains(password);
    }

}

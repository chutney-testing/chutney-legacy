package com.chutneytesting.task.ssh.sshj;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.util.StringUtils.isEmpty;

import com.chutneytesting.task.ssh.Command;
import com.chutneytesting.task.ssh.CommandResult;
import com.chutneytesting.task.ssh.Connection;
import com.chutneytesting.task.ssh.SshClient;
import java.io.IOException;
import java.io.InputStream;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class SshJClient implements SshClient {

    private final Connection connection;

    public SshJClient(Connection connection) {
        this.connection = connection;
    }

    @Override
    public CommandResult execute(Command command) throws IOException {
        SSHClient sshClient = new SSHClient();
        connect(sshClient, connection);
        authenticate(sshClient, connection);
        return executeCommand(sshClient, command);
    }

    private void connect(SSHClient client, Connection connection) throws IOException {
        client.addHostKeyVerifier((a, b, c) -> true); // TODO GVE : Add best way host key verifier to really check.
        client.connect(connection.serverHost, connection.serverPort);
    }

    private void authenticate(SSHClient client, Connection connection) throws IOException {
        if (isEmpty(connection.privateKey)) {
            loginWithPassword(client, connection.username, connection.password);
        }
        else {
            loginWithPrivateKey(client, connection.username, connection.privateKey);
        }
    }

    private void loginWithPassword(SSHClient client, String username, String password) throws UserAuthException, TransportException {
        client.authPassword(username, password);
    }

    private void loginWithPrivateKey(SSHClient client, String username, String privateKey) throws IOException {
        KeyProvider keyProvider = client.loadKeys(privateKey);
        client.authPublickey(username, keyProvider);
    }

    private CommandResult executeCommand(SSHClient sshClient, Command command) throws IOException {
        try (Session session = sshClient.startSession()) {
            Session.Command sshJCommand = session.exec(command.command);
            sshJCommand.join(command.timeout.toMilliseconds(), MILLISECONDS);

            try(InputStream is = sshJCommand.getInputStream();
                InputStream es = sshJCommand.getErrorStream()) {
                return new CommandResult(command,
                                         sshJCommand.getExitStatus(),
                                         readInputStream(is),
                                         readInputStream(es));
            }
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        return IOUtils.readFully(inputStream).toString().replaceAll("\r", "");
    }

}

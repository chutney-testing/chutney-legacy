package com.chutneytesting.task.ssh.sshj;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.util.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class SshJClient implements SshClient {

    private final Connection connection;
    private final boolean shell;

    public SshJClient(Connection connection) {
        this(connection, false);
    }

    public SshJClient(Connection connection, boolean shell) {
        this.connection = connection;
        this.shell = shell;
    }

    @Override
    public CommandResult execute(Command command) throws IOException {
        SSHClient sshClient = new SSHClient();
        connect(sshClient, connection);
        try {
            authenticate(sshClient, connection);
            return executeCommand(sshClient, command);
        } finally {
            sshClient.disconnect();
        }
    }

    private void connect(SSHClient client, Connection connection) throws IOException {
        client.addHostKeyVerifier((a, b, c) -> true); // TODO GVE : Add best way host key verifier to really check.
        client.connect(connection.serverHost, connection.serverPort);
    }

    private void authenticate(SSHClient client, Connection connection) throws IOException {
        if (isEmpty(connection.privateKey)) {
            loginWithPassword(client, connection.username, connection.password);
        } else {
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
            if (shell) {
                return shellCommand(command, session);
            } else {
                return execCommand(command, session);
            }
        }
    }

    private CommandResult shellCommand(Command command, Session session) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        session.allocateDefaultPTY();
        Session.Shell shell = session.startShell();

        new StreamCopier(shell.getInputStream(), out, LoggerFactory.DEFAULT)
            .bufSize(shell.getLocalMaxPacketSize())
            .spawn("out");

        new StreamCopier(shell.getErrorStream(), err, LoggerFactory.DEFAULT)
            .bufSize(shell.getLocalMaxPacketSize())
            .spawn("err");

        OutputStream shellOut = shell.getOutputStream();
        shellOut.write(command.command.getBytes());
        shellOut.flush();

        while (shell.getInputStream().available() > 0 && shell.getErrorStream().available() > 0) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new CommandResult(
            command,
            err.size() > 0 ? -1 : 0,
            out.toString(),
            err.toString());
    }

    private CommandResult execCommand(Command command, Session session) throws IOException {
        Session.Command sshJCommand = session.exec(command.command);
        sshJCommand.join(command.timeout.toMilliseconds(), MILLISECONDS);

        try (InputStream is = sshJCommand.getInputStream();
             InputStream es = sshJCommand.getErrorStream()) {
            return new CommandResult(command,
                sshJCommand.getExitStatus(),
                readInputStream(is),
                readInputStream(es));
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        return IOUtils.readFully(inputStream).toString().replaceAll("\r", "");
    }

}

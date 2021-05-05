package com.chutneytesting.task.ssh.sshd;

import java.io.IOException;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

public class NoShellFactory implements ShellFactory {

    private final SshServerMock sshServerMock;

    public NoShellFactory(SshServerMock sshServerMock) {
        this.sshServerMock = sshServerMock;
    }

    @Override
    public Command createShell(ChannelSession channel) throws IOException {
        return new com.chutneytesting.task.ssh.sshd.Command(sshServerMock);
    }
}

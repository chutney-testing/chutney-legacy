package com.chutneytesting.action.ssh.sshd;

import java.io.IOException;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

public class ChutneyCommandFactory implements org.apache.sshd.server.command.CommandFactory {

    private final SshServerMock mock;

    public ChutneyCommandFactory(SshServerMock mock) {
        this.mock = mock;
    }

    @Override
    public Command createCommand(ChannelSession channel, String command) throws IOException {
        return new com.chutneytesting.action.ssh.sshd.Command(mock, command);
    }
}

package com.chutneytesting.action.ssh.sshj;

import java.io.IOException;

public interface SshClient {
    CommandResult execute(Command command) throws IOException;
}

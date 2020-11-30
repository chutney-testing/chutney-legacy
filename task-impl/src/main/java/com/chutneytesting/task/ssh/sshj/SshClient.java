package com.chutneytesting.task.ssh.sshj;

import java.io.IOException;

public interface SshClient {
    CommandResult execute(Command command) throws IOException;
}

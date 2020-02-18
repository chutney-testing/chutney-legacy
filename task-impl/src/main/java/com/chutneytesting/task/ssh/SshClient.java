package com.chutneytesting.task.ssh;

import java.io.IOException;

public interface SshClient {
    CommandResult execute(Command command) throws IOException;
}

package com.chutneytesting.task.ssh;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.ssh.sshd.SshServerMock;
import java.io.IOException;

public class SshServerStopTask implements Task {

    private final Logger logger;
    private final SshServerMock sshServer;

    public SshServerStopTask(Logger logger, @Input("ssh-server") SshServerMock sshServer) {
        this.logger = logger;
        this.sshServer = sshServer;
    }


    @Override
    public TaskExecutionResult execute() {
        try {
            sshServer.stop();
            logger.info("SshServer instance " + sshServer + " closed");
            return TaskExecutionResult.ok();
        } catch (IOException ioe) {
            logger.error(ioe);
            return TaskExecutionResult.ko();
        }
    }
}

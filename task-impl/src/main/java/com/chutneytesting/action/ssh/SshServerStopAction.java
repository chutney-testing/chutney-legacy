package com.chutneytesting.action.ssh;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.ssh.sshd.SshServerMock;
import java.io.IOException;

public class SshServerStopAction implements Action {

    private final Logger logger;
    private final SshServerMock sshServer;

    public SshServerStopAction(Logger logger, @Input("ssh-server") SshServerMock sshServer) {
        this.logger = logger;
        this.sshServer = sshServer;
    }


    @Override
    public ActionExecutionResult execute() {
        try {
            sshServer.stop();
            logger.info("SshServer instance " + sshServer + " closed");
            return ActionExecutionResult.ok();
        } catch (IOException ioe) {
            logger.error(ioe);
            return ActionExecutionResult.ko();
        }
    }
}

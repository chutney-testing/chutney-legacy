/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

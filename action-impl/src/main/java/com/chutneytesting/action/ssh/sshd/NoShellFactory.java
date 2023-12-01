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

package com.chutneytesting.action.ssh.sshd;

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
        return new com.chutneytesting.action.ssh.sshd.Command(sshServerMock);
    }
}

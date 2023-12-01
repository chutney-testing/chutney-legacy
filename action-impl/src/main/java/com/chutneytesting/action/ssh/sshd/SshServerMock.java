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

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.sshd.server.SshServer;

public class SshServerMock {

    private final SshServer sshServer;

    private final List<String> stubs;
    private final List<Boolean> usedStubs;
    private final List<String> commands = new ArrayList<>();

    public SshServerMock(SshServer sshServer, List<String> stubs) {
        this.sshServer = sshServer;
        this.stubs = stubs;
        this.usedStubs = stubs.stream().map(s -> false).collect(toList());
    }

    public Optional<String> addCommand(String command) {
        commands.add(command);
        int lastCommandIndex = commands.size() - 1;
        if (lastCommandIndex < stubs.size()) {
            usedStubs.set(lastCommandIndex, true);
            return of(stubs.get(lastCommandIndex));
        }
        return empty();
    }

    public List<String> commands() {
        return unmodifiableList(commands);
    }

    public List<String> stubs() {
        return unmodifiableList(stubs);
    }

    public String command(int i) {
        return commands.get(i);
    }

    public boolean allStubsUsed() {
        return usedStubs.stream().reduce((b1, b2) -> b1 && b2).orElse(false);
    }

    public void start() throws IOException {
        sshServer.start();
    }

    public void stop() throws IOException {
        sshServer.stop();
    }

    public String host() {
        return sshServer.getHost();
    }

    public int port() {
        return sshServer.getPort();
    }

    public boolean isStarted() {
        return sshServer.isStarted();
    }

    public boolean isClosed() {
        return sshServer.isClosed();
    }

    public boolean isOpen() {
        return sshServer.isOpen();
    }
}

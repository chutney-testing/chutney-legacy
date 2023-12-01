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

package com.chutneytesting.action.ssh.sshj;


import com.chutneytesting.action.spi.time.Duration;
import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.util.Optional;

public class Command {

    private static final Duration DEFAULT_DURATION = Duration.parse("5000 ms");

    public final String command;
    public final Duration timeout;

    public Command(String command) {
        this.command = command;
        this.timeout = DEFAULT_DURATION;
    }

    Command(String command, String timeout) {
        this.command = command;
        this.timeout = Optional.ofNullable(timeout).map(Duration::parse).orElse(DEFAULT_DURATION);
    }

    CommandResult executeWith(SshClient sshClient) throws IOException {
        return sshClient.execute(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("command",command)
            .add("timeout",timeout)
            .toString();
    }
}

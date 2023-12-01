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

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class CommandResult {

    public final Command command;
    public final int exitCode;
    public final String stdout;
    public final String stderr;

    public CommandResult(Command command, int exitCode, String stdout, String stderr) {
        this.command = command;
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandResult that = (CommandResult) o;
        return exitCode == that.exitCode &&
            Objects.equals(stdout, that.stdout) &&
            Objects.equals(stderr, that.stderr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exitCode, stdout, stderr);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("command",command)
            .add("exitCode",exitCode)
            .add("stdout",stdout)
            .add("stderr",stderr)
            .toString();
    }

}

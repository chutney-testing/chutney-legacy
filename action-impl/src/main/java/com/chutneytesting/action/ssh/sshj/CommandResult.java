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

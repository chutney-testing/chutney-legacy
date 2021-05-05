package com.chutneytesting.task.ssh;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshj.CommandResult;
import com.chutneytesting.task.ssh.sshj.Commands;
import com.chutneytesting.task.ssh.sshj.Connection;
import com.chutneytesting.task.ssh.sshj.SshClient;
import com.chutneytesting.task.ssh.sshj.SshJClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SshClientTask implements Task {

    private final Target target;
    private final Logger logger;
    private final Commands commands;
    private final CHANNEL channel;

    public SshClientTask(Target target, Logger logger, @Input("commands") List<Object> commands, @Input("channel") String channel) {
        this.target = target;
        this.logger = logger;
        this.commands = Commands.from(commands);
        this.channel = ofNullable(channel).map(CHANNEL::from).orElse(CHANNEL.COMMAND);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Connection connection = Connection.from(target);
            SshClient sshClient = new SshJClient(connection, CHANNEL.SHELL.equals(channel));

            List<CommandResult> commandResults = commands.executeWith(sshClient);

            Map<String, List<CommandResult>> taskResult = new HashMap<>();
            taskResult.put("results", commandResults);

            return TaskExecutionResult.ok(taskResult);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

    private enum CHANNEL {
        COMMAND, SHELL;

        public static CHANNEL from(String channel) {
            for (CHANNEL value : CHANNEL.values()) {
                if (value.name().equalsIgnoreCase((channel))) {
                    return value;
                }
            }
            return COMMAND;
        }
    }

}

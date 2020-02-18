package com.chutneytesting.task.ssh;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.sshj.SshJClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SshClientTask implements Task {

    private final Target target;
    private final Logger logger;
    private final Commands commands;

    public SshClientTask(Target target, Logger logger, @Input("commands") List<Object> commands) {
        this.target = target;
        this.logger = logger;
        this.commands = Commands.from(commands);
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Connection connection = Connection.from(target);
            SshClient sshClient = new SshJClient(connection);

            List<CommandResult> commandResults = commands.executeWith(sshClient);

            Map<String, List<CommandResult>> taskResult = new HashMap<>();
            taskResult.put("results", commandResults);

            return TaskExecutionResult.ok(taskResult);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

}

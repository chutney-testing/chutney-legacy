package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.validation.Validator;
import com.chutneytesting.task.ssh.sshj.CommandResult;
import com.chutneytesting.task.ssh.sshj.Commands;
import com.chutneytesting.task.ssh.sshj.SshClient;
import com.chutneytesting.task.ssh.sshj.SshJClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SshClientTask implements Task {

    private final Target target;
    private final Logger logger;
    private final List<Object> commands;
    private final String channel;

    public SshClientTask(Target target, Logger logger, @Input("commands") List<Object> commands, @Input("channel") String channel) {
        this.target = target;
        this.logger = logger;
        this.commands = commands;
        this.channel = ofNullable(channel).orElse(CHANNEL.COMMAND.name());
    }

    @Override
    public List<String> validateInputs() {
        Validator<List<Object>> commandsValidator = notEmptyListValidation(this.commands, "commands")
            .validate(Commands::from, noException -> true, "Syntax is a List of String or a List of {command: \"xxx\", timeout:\"10 s\"} Json");
        return getErrorsFrom(
            targetValidation(target),
            commandsValidator
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            Connection connection = Connection.from(target);
            boolean isSshChannel = CHANNEL.SHELL.equals(CHANNEL.from(this.channel));
            SshClient sshClient = new SshJClient(connection, isSshChannel, logger);

            List<CommandResult> commandResults = Commands.from(this.commands).executeWith(sshClient);

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

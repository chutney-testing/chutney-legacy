package com.chutneytesting.task.ssh;

import static java.util.Objects.requireNonNull;
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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

public class SshClientTask implements Task {

    private final Target target;
    private final Logger logger;
    private final Commands commands;
    private final CHANNEL channel;

    public SshClientTask(Target target, Logger logger, @Input("commands") List<Object> commands, @Input("channel") String channel) {
        this.target = target;
        this.logger = requireNonNull(logger, "Logger cannot be null...");
        this.commands = Commands.from(commands);
        this.channel = ofNullable(channel).map(CHANNEL::from).orElse(CHANNEL.COMMAND);
    }

    @Override
    public TaskExecutionResult execute() {
        if (!couldExecute()) {
            return TaskExecutionResult.ko();
        }

        try {
            Connection connection = Connection.from(target);
            SshClient sshClient = new SshJClient(connection, CHANNEL.SHELL.equals(channel), logger);

            List<CommandResult> commandResults = commands.executeWith(sshClient);

            Map<String, List<CommandResult>> taskResult = new HashMap<>();
            taskResult.put("results", commandResults);

            return TaskExecutionResult.ok(taskResult);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

    private boolean couldExecute() {
        boolean couldExecute;

        // Validate target
        couldExecute = checkForError(target,
            List.of(
                Pair.of(Objects::isNull, "Target is mandatory"),
                Pair.of(t -> t.url() == null || t.url().isBlank(), "Target url is mandatory"),
                Pair.of(t -> {
                    try {
                        t.getUrlAsURI();
                        return false;
                    } catch (IllegalStateException ise) {
                        return true;
                    }
                }, "Target url is not valid"),
                Pair.of(t -> {
                    URI uri = target.getUrlAsURI();
                    return uri.getHost() == null || uri.getHost().isBlank();
                }, "Target url has an undefined host")
            )
        );

        // Validate commands
        couldExecute &= checkForError(commands,
            List.of(
                Pair.of(c -> c.all.isEmpty(), "No commands provided")
            )
        );

        return couldExecute;
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

    private <T> boolean checkForError(T objectToCheck, List<Pair<Predicate<T>, String>> checksWithMessages) {
        for (Pair<Predicate<T>, String> checkWithMessage : checksWithMessages) {
            if (checkWithMessage.getLeft().test(objectToCheck)) {
                logger.error(checkWithMessage.getRight());
                return false;
            }
        }
        return true;
    }

}

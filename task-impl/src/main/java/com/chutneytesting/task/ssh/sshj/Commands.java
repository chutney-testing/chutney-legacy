package com.chutneytesting.task.ssh.sshj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commands {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

    final List<Command> all;

    private Commands(List<Command> commands) {
        this.all = commands;
    }

    public static Commands from(List<Object> commands) {
        List<Command> cmds = commands.stream()
            .map(Commands::buildCommand)
            .collect(Collectors.toList());

        if (cmds.isEmpty()) {
            throw new IllegalArgumentException("No command defined");
        }

        return new Commands(cmds);
    }

    @SuppressWarnings("unchecked")
    private static Command buildCommand(Object command) {
        if (command instanceof String) {
            return new Command((String) command);
        }

        if (command instanceof Map) {
            return new Command(((Map<String, String>) command).get("command"),
                ((Map<String, String>) command).get("timeout"));
        }

        throw new IllegalStateException("Unable to understand command: " + command.toString());
    }

    public List<CommandResult> executeWith(SshClient sshClient) throws IOException {
        List<CommandResult> results = new ArrayList<>();

        for (Command command : this.all) {
            try {
                LOGGER.debug("COMMANDS :: {}", command);
                CommandResult result = command.executeWith(sshClient);
                results.add(result);
                LOGGER.debug("RESULT :: {}", result);
            } catch (IOException e) {
                throw e;
            }
        }

        return results;
    }

}

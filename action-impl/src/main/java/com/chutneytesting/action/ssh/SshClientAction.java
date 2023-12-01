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

package com.chutneytesting.action.ssh;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notEmptyListValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.validation.Validator;
import com.chutneytesting.action.ssh.sshj.CommandResult;
import com.chutneytesting.action.ssh.sshj.Commands;
import com.chutneytesting.action.ssh.sshj.SshClient;
import com.chutneytesting.action.ssh.sshj.SshJClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SshClientAction implements Action {

    private final Target target;
    private final Logger logger;
    private final List<Object> commands;
    private final String channel;

    public SshClientAction(Target target, Logger logger, @Input("commands") List<Object> commands, @Input("channel") String channel) {
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
    public ActionExecutionResult execute() {
        try {
            Connection connection = Connection.from(target);
            boolean isSshChannel = CHANNEL.SHELL.equals(CHANNEL.from(this.channel));
            SshClient sshClient = new SshJClient(connection, isSshChannel, logger);

            List<CommandResult> commandResults = Commands.from(this.commands).executeWith(sshClient);

            Map<String, List<CommandResult>> actionResult = new HashMap<>();
            actionResult.put("results", commandResults);

            return ActionExecutionResult.ok(actionResult);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
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

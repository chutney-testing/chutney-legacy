package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.ssh.scp.ScpClient;
import com.chutneytesting.task.ssh.scp.ScpClientImpl;
import com.chutneytesting.task.ssh.sshj.Connection;
import java.io.IOException;
import java.util.List;

public class ScpUploadTask implements Task {

    private final Target target;
    private final Logger logger;
    private final String local;
    private final String remote;

    public ScpUploadTask(Target target, Logger logger, @Input("source") String source, @Input("destination") String destination) {
        this.target = target;
        this.logger = logger;
        this.local = source;
        this.remote = destination;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(local, "local source"),
            notBlankStringValidation(remote, "remote destination"),
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            ScpClient scpClient = ScpClientImpl.builder()
                .withConnection(Connection.from(target))
                .build();

            scpClient.upload(local, remote);
            scpClient.close();

            return TaskExecutionResult.ok();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }
}


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

public class ScpDownloadTask implements Task {

    private final Target target;
    private final Logger logger;
    private final String remote;
    private final String local;

    public ScpDownloadTask(Target target, Logger logger, @Input("source") String source, @Input("destination") String destination) {
        this.target = target;
        this.logger = logger;
        this.remote = source;
        this.local = destination;
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(remote, "remote source"),
            notBlankStringValidation(local, "local destination"),
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try {
            ScpClient scpClient = ScpClientImpl.builder()
                .withConnection(Connection.from(target))
                .build();

            scpClient.download(remote, local);
            scpClient.close();

            return TaskExecutionResult.ok();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

}

package com.chutneytesting.task.ssh;

import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.durationValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.task.spi.validation.TaskValidatorsUtils.targetValidation;
import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.chutneytesting.task.ssh.scp.ScpClient;
import com.chutneytesting.task.ssh.sftp.ChutneySftpClient;
import com.chutneytesting.task.ssh.sftp.SftpClientImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SftpListDirTask implements Task {

    private final Target target;
    private final Logger logger;
    private final String directory;
    private final String timeout;

    public SftpListDirTask(Target target, Logger logger, @Input("directory") String directory, @Input("timeout") String timeout) {
        this.target = target;
        this.logger = logger;
        this.directory = directory;
        this.timeout = defaultIfEmpty(timeout, ScpClient.DEFAULT_TIMEOUT);
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(directory, "remote directory path"),
            durationValidation(timeout, "timeout"),
            targetValidation(target)
        );
    }

    @Override
    public TaskExecutionResult execute() {
        try (ChutneySftpClient client = SftpClientImpl.buildFor(target, Duration.parseToMs(timeout), logger)) {
            List<String> files = client.listDirectory(directory);
            Map<String, List<String>> taskResult = new HashMap<>(1);
            taskResult.put("files", files);
            return TaskExecutionResult.ok(taskResult);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return TaskExecutionResult.ko();
        }
    }

}


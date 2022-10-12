package com.chutneytesting.action.ssh;

import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.durationValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.notBlankStringValidation;
import static com.chutneytesting.action.spi.validation.ActionValidatorsUtils.targetValidation;
import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.ssh.SshClientFactory.DEFAULT_TIMEOUT;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.action.spi.time.Duration;
import com.chutneytesting.action.ssh.sftp.ChutneySftpClient;
import com.chutneytesting.action.ssh.sftp.SftpClientImpl;
import java.util.List;
import java.util.Map;

public class SftpListDirAction implements Action {

    private final Target target;
    private final Logger logger;
    private final String directory;
    private final String timeout;

    public SftpListDirAction(Target target, Logger logger, @Input("directory") String directory, @Input("timeout") String timeout) {
        this.target = target;
        this.logger = logger;
        this.directory = directory;
        this.timeout = defaultIfEmpty(timeout, DEFAULT_TIMEOUT);
    }

    @Override
    public List<String> validateInputs() {
        return getErrorsFrom(
            notBlankStringValidation(directory, "directory"),
            durationValidation(timeout, "timeout"),
            targetValidation(target)
        );
    }

    @Override
    public ActionExecutionResult execute() {
        try (ChutneySftpClient client = SftpClientImpl.buildFor(target, Duration.parseToMs(timeout), logger)) {
            List<String> files = client.listDirectory(directory);
            Map<String, List<String>> actionResult = Map.of("files", files);
            return ActionExecutionResult.ok(actionResult);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ActionExecutionResult.ko();
        }
    }

}


package com.chutneytesting.task.http;

import static com.chutneytesting.task.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.task.spi.validation.Validator.of;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.validation.Validator;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Objects;

public class HttpsServerStopTask implements Task {

    private Logger logger;

    private WireMockServer httpsServer;

    public HttpsServerStopTask(Logger logger, @Input("https-server") WireMockServer httpsServer) {
        this.logger = logger;
        this.httpsServer = httpsServer;
    }

    @Override
    public List<String> validateInputs() {
        Validator<WireMockServer> httpsServerValidation = of(httpsServer)
            .validate(Objects::nonNull, "No httpsServer provided");
        return getErrorsFrom(httpsServerValidation);
    }

    @Override
    public TaskExecutionResult execute() {
        logger.info("HttpsServer instance " + httpsServer + "closed");
        httpsServer.stop();
        return TaskExecutionResult.ok();
    }
}

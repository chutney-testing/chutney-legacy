package com.chutneytesting.task.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;

public class HttpsServerStopTask implements Task {

    private Logger logger;

    private WireMockServer httpsServer;

    public HttpsServerStopTask(Logger logger,  @Input("https-server") WireMockServer httpsServer) {
        this.logger = logger;
        this.httpsServer = httpsServer;
    }


    @Override
    public TaskExecutionResult execute() {
        logger.info("HttpsServer instance " + httpsServer + "closed");
        httpsServer.stop();
        return TaskExecutionResult.ok();
    }
}

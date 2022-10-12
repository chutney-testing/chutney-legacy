package com.chutneytesting.action.http;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Objects;

public class HttpsServerStopAction implements Action {

    private Logger logger;

    private WireMockServer httpsServer;

    public HttpsServerStopAction(Logger logger, @Input("https-server") WireMockServer httpsServer) {
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
    public ActionExecutionResult execute() {
        logger.info("HttpsServer instance " + httpsServer + "closed");
        httpsServer.stop();
        return ActionExecutionResult.ok();
    }
}

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

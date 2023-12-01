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

package com.chutneytesting.action.jakarta;

import static com.chutneytesting.action.spi.validation.Validator.getErrorsFrom;
import static com.chutneytesting.action.spi.validation.Validator.of;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.validation.Validator;
import java.util.List;
import java.util.Objects;
import org.apache.activemq.artemis.core.server.ActiveMQServer;

public class JakartaBrokerStopAction implements Action {

    private final Logger logger;
    private final ActiveMQServer brokerService;

    public JakartaBrokerStopAction(Logger logger, @Input("jakarta-broker-service") ActiveMQServer brokerService) {
        this.logger = logger;
        this.brokerService = brokerService;
    }

    @Override
    public List<String> validateInputs() {
        Validator<ActiveMQServer> jmsBrokerValidation = of(brokerService)
            .validate(Objects::nonNull, "No jakarta-broker-service provided");
        return getErrorsFrom(jmsBrokerValidation);
    }

    @Override
    public ActionExecutionResult execute() {
        logger.info("Call jms broker shutdown");
        try {
            brokerService.stop();
            return ActionExecutionResult.ok();
        } catch (Exception e) {
            logger.error(e);
            return ActionExecutionResult.ko();
        }
    }
}

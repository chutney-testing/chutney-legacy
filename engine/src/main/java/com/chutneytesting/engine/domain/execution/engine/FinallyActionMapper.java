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

package com.chutneytesting.engine.domain.execution.engine;

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.chutneytesting.action.spi.FinallyAction;

class FinallyActionMapper {

    StepDefinition toStepDefinition(FinallyAction finallyAction) {
        return new StepDefinition(
            finallyAction.name(),
            finallyAction.target()
                .orElse(TargetImpl.NONE),
            finallyAction.type(),
            finallyAction.strategyType()
                .map(st -> new StepStrategyDefinition(st, new StrategyProperties(finallyAction.strategyProperties().orElse(emptyMap()))))
                .orElse(null),
            finallyAction.inputs(),
            null,
            null,
            finallyAction.validations()
        );
    }
}

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

package com.chutneytesting.action.assertion.compare;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Objects;

public class CompareEqualsAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        if (Objects.equals(actual, expected)) {
            logger.info("[" + expected + "] EQUALS [" + actual + "]");
            return ActionExecutionResult.ok();
        } else {
            logger.error("[" + expected + "] NOT EQUALS [" + actual + "]");
            return ActionExecutionResult.ko();
        }
    }
}

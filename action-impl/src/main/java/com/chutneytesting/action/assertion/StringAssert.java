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

package com.chutneytesting.action.assertion;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Objects;

// TODO replace by an assert on context compatible with language-expressions (spel, ognl, velocity, etc.)
@Deprecated
public class StringAssert implements Action {

    private final Logger logger;
    private final String actual;
    private final String expected;

    public StringAssert(Logger logger, @Input("document") String actual, @Input("expected") String expected) {
        this.logger = logger;
        this.actual = actual;
        this.expected = expected;
    }

    @Override
    public ActionExecutionResult execute() {
        if (!Objects.equals(actual, expected)) {
            logger.error("Expected value [" + expected + "], but found [" + actual + "]"
                    + " - Action string-assert is deprecated. Use assert-true instead."
            );
            return ActionExecutionResult.ko();
        } else {
            logger.info("Found expected value [" + actual + "]"
                   + " - Action string-assert is deprecated. Use assert-true instead."
            );
            return ActionExecutionResult.ok();
        }
    }

}

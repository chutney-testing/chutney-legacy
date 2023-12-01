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

import com.chutneytesting.action.assertion.compare.CompareActionFactory;
import com.chutneytesting.action.assertion.compare.CompareExecutor;
import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;

public class CompareAction implements Action {

    private final Logger logger;
    private final String actual;
    private final String expected;
    private final String mode;

    public CompareAction(Logger logger, @Input("actual") String actual, @Input("expected") String expected, @Input("mode") String mode) {
        this.logger = logger;
        this.actual = actual;
        this.expected = expected;
        this.mode = mode;
    }

    @Override
    public ActionExecutionResult execute() {
        CompareExecutor compareExecutor = CompareActionFactory.createCompareAction(mode);
        return compareExecutor.compare(logger, actual, expected);
    }
}

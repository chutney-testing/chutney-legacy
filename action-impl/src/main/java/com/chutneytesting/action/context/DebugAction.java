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

package com.chutneytesting.action.context;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Input;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DebugAction implements Action {

    private final Logger logger;
    private final Map<String, Object> inputs;
    private final List<String> filter;

    public DebugAction(Logger logger, Map<String, Object> inputs, @Input("filters") List<String> filter) {
        this.logger = logger;
        this.inputs = inputs;
        this.filter = Optional.ofNullable(filter).orElseGet(Collections::emptyList);
    }

    @Override
    public ActionExecutionResult execute() {
        inputs.entrySet().stream()
            .filter(entry -> filter.isEmpty() || filter.contains(entry.getKey()))
            .forEach(entry -> logger.info(entry.getKey() + " : [" + entry.getValue() + "]"));
        return ActionExecutionResult.ok();
    }
}

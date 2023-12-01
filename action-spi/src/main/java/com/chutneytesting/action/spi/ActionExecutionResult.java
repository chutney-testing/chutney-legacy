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

package com.chutneytesting.action.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of a {@link Action} execution.<br>
 * Can be either:
 * <ul>
 * <li>{@link Status#Success} with outputs (key / value)</li>
 * <li>{@link Status#Failure}</li>
 * </ul>
 */
public class ActionExecutionResult {
    public final Status status;
    public final Map<String, Object> outputs;

    private ActionExecutionResult(Status status, Map<String, ?> outputs) {
        this.status = status;
        this.outputs = Collections.unmodifiableMap(outputs);
    }

    private ActionExecutionResult(Status status) {
        this(status, Collections.emptyMap());
    }

    public static ActionExecutionResult ok() {
        return new ActionExecutionResult(Status.Success);
    }

    public static ActionExecutionResult ok(Map<String, ?> outputs) {
        return new ActionExecutionResult(Status.Success, outputs);
    }

    public static ActionExecutionResult ok(String key, Object value) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(key, value);
        return new ActionExecutionResult(Status.Success, outputs);
    }

    public static ActionExecutionResult ko() {
        return new ActionExecutionResult(Status.Failure);
    }

    public static ActionExecutionResult ko(Map<String, ?> outputs) {
        return new ActionExecutionResult(Status.Failure, outputs);
    }

    public enum Status {
        Success, Failure
    }
}

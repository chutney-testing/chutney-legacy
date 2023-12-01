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

package com.chutneytesting.action.http.domain;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

public class HttpAction {

    public static ActionExecutionResult httpCall(Logger logger, Supplier<ResponseEntity<String>> caller) {
        try {
            ResponseEntity<String> response = caller.get();
            logger.info("HTTP call status :" + response.getStatusCode().value());
            return ActionExecutionResult.ok(toOutputs(response));
        }
        catch (ResourceAccessException e) {
            logger.error("HTTP call failed during execution: " + e.getMessage());
            return ActionExecutionResult.ko();
        }
    }

    private static Map<String, Object> toOutputs(ResponseEntity<String> response) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put("status", response.getStatusCode().value());
        outputs.put("body", response.getBody());
        outputs.put("headers", response.getHeaders());
        return outputs;
    }
}

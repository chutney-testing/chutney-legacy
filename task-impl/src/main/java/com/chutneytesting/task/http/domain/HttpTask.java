package com.chutneytesting.task.http.domain;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

public class HttpTask {

    public static TaskExecutionResult httpCall(Logger logger, Supplier<ResponseEntity<String>> caller) {
        try {
            ResponseEntity<String> response = caller.get();
            return TaskExecutionResult.ok(toOutputs(response));
        }
        catch (ResourceAccessException e) {
            logger.error("HTTP call failed during execution: " + e.getMessage());
            return TaskExecutionResult.ko();
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

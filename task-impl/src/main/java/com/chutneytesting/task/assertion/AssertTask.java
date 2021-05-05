package com.chutneytesting.task.assertion;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.List;
import java.util.Map;

/**
 * Input are evaluated (SPeL) before entering the task
 */
public class AssertTask implements Task {

    private final Logger logger;
    private final List<Map<String, Boolean>> asserts;

    public AssertTask(Logger logger, @Input("asserts") List<Map<String, Boolean>> asserts) {
        this.logger = logger;
        this.asserts = asserts;
    }

    @Override
    public TaskExecutionResult execute() {
        boolean result = asserts.stream().allMatch(l -> l.entrySet().stream()
            .map(e -> {
                if ("assert-true".equals(e.getKey())) {
                    if (e.getValue()) {
                        logger.info("assert ok");
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    logger.error("Unknown assert type [" + e.getKey() + "]");
                    return Boolean.FALSE;
                }
            })
            .allMatch(r -> r)
        );
        return result ? TaskExecutionResult.ok() : TaskExecutionResult.ko();
    }
}

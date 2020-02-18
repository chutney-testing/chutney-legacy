package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Map;

public class ContextPutTask implements Task {

    private final Logger logger;
    private final Map<String, Object> entries;

    public ContextPutTask(Logger logger, @Input("entries") Map<String, Object> entries) {
        this.logger = logger;
        this.entries = entries;

        assertExecutionIsPossible();
    }

    @Override
    public TaskExecutionResult execute() {
        entries.forEach((k, v) -> logger.info("Adding to context " + k + " = " + v + " (" + v.getClass().getSimpleName() + ")"));
        return TaskExecutionResult.ok(entries);
    }

    private void assertExecutionIsPossible() {
        if (entries == null) {
            throw new IllegalArgumentException("Entries to put in context not found.");
        }

        if (entries.isEmpty()) {
            logger.info("Nothing to put in context");
        }
    }
}

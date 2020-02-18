package com.chutneytesting.task.assertion;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.assertion.compareTask.CompareExecutor;
import com.chutneytesting.task.assertion.compareTask.CompareTaskFactory;

public class CompareTask implements Task {

    private final Logger logger;
    private final String actual;
    private final String expected;
    private final String mode;

    public CompareTask(Logger logger, @Input("actual") String actual, @Input("expected") String expected, @Input("mode") String mode) {
        this.logger = logger;
        this.actual = actual;
        this.expected = expected;
        this.mode = mode;
    }

    @Override
    public TaskExecutionResult execute() {
        CompareExecutor compareExecutor = CompareTaskFactory.createCompareTask(mode);
        return compareExecutor.compare(logger, actual, expected);
    }
}

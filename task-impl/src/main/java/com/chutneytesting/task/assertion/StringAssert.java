package com.chutneytesting.task.assertion;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Objects;

// TODO replace by an assert on context compatible with language-expressions (spel, ognl, velocity, etc.)
@Deprecated
public class StringAssert implements Task {

    private final Logger logger;
    private final String actual;
    private final String expected;

    public StringAssert(Logger logger, @Input("document") String actual, @Input("expected") String expected) {
        this.logger = logger;
        this.actual = actual;
        this.expected = expected;
    }

    @Override
    public TaskExecutionResult execute() {
        if (!Objects.equals(actual, expected)) {
            logger.error("Expected value [" + expected + "], but found [" + actual + "]"
                    + " - Task string-assert is deprecated. Use assert-true instead."
            );
            return TaskExecutionResult.ko();
        } else {
            logger.info("Found expected value [" + actual + "]"
                   + " - Task string-assert is deprecated. Use assert-true instead."
            );
            return TaskExecutionResult.ok();
        }
    }

}

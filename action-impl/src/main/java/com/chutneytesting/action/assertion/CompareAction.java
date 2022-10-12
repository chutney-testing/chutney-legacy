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

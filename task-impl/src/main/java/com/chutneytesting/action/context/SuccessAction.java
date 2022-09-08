package com.chutneytesting.action.context;

import com.chutneytesting.action.spi.Action;
import com.chutneytesting.action.spi.ActionExecutionResult;

public class SuccessAction implements Action {

    public SuccessAction() {
    }

    @Override
    public ActionExecutionResult execute() {
        return ActionExecutionResult.ok();
    }
}

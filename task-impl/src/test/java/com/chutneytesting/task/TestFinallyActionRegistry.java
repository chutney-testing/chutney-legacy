package com.chutneytesting.task;

import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.injectable.FinallyActionRegistry;
import java.util.ArrayList;
import java.util.List;

public class TestFinallyActionRegistry implements FinallyActionRegistry {

    public final List<FinallyAction> finallyActions = new ArrayList<>();

    @Override
    public void registerFinallyAction(FinallyAction finallyAction) {
        finallyActions.add(finallyAction);
    }
}

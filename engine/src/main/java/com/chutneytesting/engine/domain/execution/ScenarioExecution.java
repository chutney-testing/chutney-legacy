package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.action.spi.FinallyAction;
import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ScenarioExecution {

    private final List<FinallyAction> finallyActions = new ArrayList<>();
    private final ActionsConfiguration actionConfiguration;
    public final long executionId;

    private boolean pause = false;
    private boolean stop = false;

    private Disposable endExecutionSubscriber;

    public static ScenarioExecution createScenarioExecution(ActionsConfiguration actionConfiguration) {
        long executionId = UUID.randomUUID().getMostSignificantBits();
        return new ScenarioExecution(executionId, actionConfiguration);
    }

    private ScenarioExecution(long executionId, ActionsConfiguration actionConfiguration) {
        this.executionId = executionId;
        this.actionConfiguration = actionConfiguration;

        final Disposable pauseSubscriber = RxBus.getInstance()
            .registerOnExecutionId(PauseExecutionAction.class, executionId, e -> this.pause());
        final Disposable stopSubscriber = RxBus.getInstance()
            .registerOnExecutionId(StopExecutionAction.class, executionId, e -> this.stop());
        final Disposable resumeSubscriber = RxBus.getInstance()
            .registerOnExecutionId(ResumeExecutionAction.class, executionId, e -> this.resume());

        endExecutionSubscriber = RxBus.getInstance().registerOnExecutionId(EndScenarioExecutionEvent.class, executionId, e -> {
            pauseSubscriber.dispose();
            stopSubscriber.dispose();
            resumeSubscriber.dispose();
            endExecutionSubscriber.dispose();
        });
    }

    public void registerFinallyAction(FinallyAction finallyAction) {
        finallyActions.add(finallyAction);
    }

    public void initFinallyActionExecution() {
        this.stop = false; // In case of a stopped scenario, we should set it to false in order to execute finally actions
    }

    public void waitForRestart() {
        while (pause) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean hasToPause() {
        return pause;
    }

    public boolean hasToStop() {
        return stop;
    }

    private void pause() {
        pause = true;
    }

    private void stop() {
        stop = true;
    }

    private void resume() {
        pause = false;
    }

    public ActionsConfiguration getActionsConfiguration() {
        return actionConfiguration;
    }

    public List<FinallyAction> finallyActions() {
        return finallyActions;
    }
}

package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.scenario.StepBuilder;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.task.spi.FinallyAction;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioExecution {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioExecution.class);
    private final List<FinallyAction> finallyActions = new ArrayList<>();

    public final long executionId;

    private boolean pause = false;
    private boolean stop = false;

    private Disposable endExecutionSubscriber;

    public static ScenarioExecution createScenarioExecution() {
        long executionId = UUID.randomUUID().getMostSignificantBits();
        return new ScenarioExecution(executionId);
    }

    private ScenarioExecution(long executionId) {
        this.executionId = executionId;

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

    /**
     * Make a copy of registered {@link FinallyAction} to avoid infinite loop
     * with {@link FinallyAction} registering {@link FinallyAction}.
     */
    public void executeFinallyActions(ScenarioContext scenarioContext, StepBuilder stepBuilder) {
        this.stop = false; // In case of a stopped scenario, we should set it to false in order to execute finally actions
        List<FinallyAction> finallyActionsSnapshot = new ArrayList<>(this.finallyActions);
        Collections.reverse(finallyActionsSnapshot);
        for (FinallyAction finallyAction : Collections.unmodifiableList(finallyActionsSnapshot)) {
            try {
                Step step = stepBuilder.buildStep(finallyAction);
                step.execute(this, scenarioContext);
            } catch (RuntimeException e) {
                LOGGER.error("Error when executing finallyActions", e);
            }
        }
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


}

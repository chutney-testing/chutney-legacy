package com.chutneytesting.engine.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import org.junit.Before;
import org.junit.Test;

public class ExecutionManagerTest {

    private ExecutionManager em = new ExecutionManager();
    ScenarioExecution execution = ScenarioExecution.createScenarioExecution();

    @Before
    public void setUp() throws Exception {
        Step step = mock(Step.class);
        RxBus.getInstance().post(new StartScenarioExecutionEvent(execution, step));
    }

    @Test
    public void pauseAndRestartExecution() {
        assertThat(execution.hasToPause()).isFalse();
        em.pauseExecution(execution.executionId);
        assertThat(execution.hasToPause()).isTrue();
        em.resumeExecution(execution.executionId);
        assertThat(execution.hasToPause()).isFalse();
    }

    @Test
    public void stopExecution() {
        assertThat(execution.hasToStop()).isFalse();
        em.stopExecution(execution.executionId);
        assertThat(execution.hasToStop()).isTrue();
    }
}

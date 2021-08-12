package com.chutneytesting.engine.domain.event;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

import com.chutneytesting.engine.domain.execution.ExecutionManager;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExecutionManagerTest {

    private ExecutionManager sut;
    private ScenarioExecution execution;

    @BeforeEach
    public void setUp() throws Exception {
        sut = new ExecutionManager();
        execution = ScenarioExecution.createScenarioExecution(null);
        Step step = mock(Step.class);
        RxBus.getInstance().post(new StartScenarioExecutionEvent(execution, step));
    }

    @Test
    public void pauseAndRestartExecution() {
        assertThat(execution.hasToPause()).isFalse();

        sut.pauseExecution(execution.executionId);
        await().atMost(1, SECONDS).untilAsserted(() ->
            assertThat(execution.hasToPause()).isTrue()
        );

        sut.resumeExecution(execution.executionId);
        await().atMost(1, SECONDS).untilAsserted(() ->
            assertThat(execution.hasToPause()).isFalse()
        );
    }

    @Test
    public void stopExecution() {
        assertThat(execution.hasToStop()).isFalse();
        sut.stopExecution(execution.executionId);
        await().atMost(1, SECONDS).untilAsserted(() ->
            assertThat(execution.hasToStop()).isTrue()
        );
    }
}

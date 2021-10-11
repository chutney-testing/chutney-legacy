package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.RxBus.getInstance;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import org.junit.jupiter.api.Test;

public class ScenarioExecutionTest {

    @Test
    public void events_should_change_execution_state() {

        // Init
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);
        assertThat(scenarioExecution.hasToPause()).isFalse();
        assertThat(scenarioExecution.hasToStop()).isFalse();

        // Pause
        getInstance().post(new PauseExecutionAction(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isTrue();
                assertThat(scenarioExecution.hasToStop()).isFalse();
            }
        );

        // Resume
        getInstance().post(new ResumeExecutionAction(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isFalse();
                assertThat(scenarioExecution.hasToStop()).isFalse();
            }
        );

        // Stop
        getInstance().post(new StopExecutionAction(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isFalse();
                assertThat(scenarioExecution.hasToStop()).isTrue();
            }
        );
    }
}

/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.RxBus.getInstance;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.command.PauseExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.ResumeExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.StopExecutionCommand;
import org.junit.jupiter.api.Test;

public class ScenarioExecutionTest {

    @Test
    public void events_should_change_execution_state() {

        // Init
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution(null);
        assertThat(scenarioExecution.hasToPause()).isFalse();
        assertThat(scenarioExecution.hasToStop()).isFalse();

        // Pause
        getInstance().post(new PauseExecutionCommand(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isTrue();
                assertThat(scenarioExecution.hasToStop()).isFalse();
            }
        );

        // Resume
        getInstance().post(new ResumeExecutionCommand(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isFalse();
                assertThat(scenarioExecution.hasToStop()).isFalse();
            }
        );

        // Stop
        getInstance().post(new StopExecutionCommand(scenarioExecution.executionId));
        await().atMost(1, SECONDS).untilAsserted(() -> {
                assertThat(scenarioExecution.hasToPause()).isFalse();
                assertThat(scenarioExecution.hasToStop()).isTrue();
            }
        );
    }
}

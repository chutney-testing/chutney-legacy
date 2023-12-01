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

package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.engine.domain.execution.command.PauseExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.ResumeExecutionCommand;
import com.chutneytesting.engine.domain.execution.command.StopExecutionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionManager.class);

    public ExecutionManager() {
    }

    public void pauseExecution(Long executionId) {
        LOGGER.info("Pause requested for " + executionId);
        RxBus.getInstance().post(new PauseExecutionCommand(executionId));
    }

    public void resumeExecution(Long executionId) {
        LOGGER.info("Resume requested for " + executionId);
        RxBus.getInstance().post(new ResumeExecutionCommand(executionId));
    }

    public void stopExecution(Long executionId) {
        LOGGER.info("Stop requested for " + executionId);
        RxBus.getInstance().post(new StopExecutionCommand(executionId));
    }
}

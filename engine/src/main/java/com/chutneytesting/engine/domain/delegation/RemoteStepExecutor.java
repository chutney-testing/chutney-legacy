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

package com.chutneytesting.engine.domain.delegation;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.Environment;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import org.springframework.util.Assert;

public class RemoteStepExecutor implements StepExecutor {

    private final DelegationClient delegationClient;
    private final NamedHostAndPort agentInfo;

    public RemoteStepExecutor(DelegationClient delegationClient, NamedHostAndPort agentInfo) {
        this.delegationClient = delegationClient;
        this.agentInfo = agentInfo;
    }

    @Override
    public void execute(ScenarioExecution scenarioExecution, Target target, Step step) {
        try {
            StepExecutionReport remoteReport = delegationClient.handDown(step, agentInfo);

            guardFromIllegalReport(remoteReport);

            step.updateContextFrom(remoteReport);

            // TODO update ScenarioExecution with registered FinallyAction

        } catch (CannotDelegateException e) {
            step.failure(e);
        }
    }

    private void guardFromIllegalReport(StepExecutionReport remoteReport) {
        Assert.notNull(remoteReport.evaluatedInputs, "EvaluatedInputs are null after delegation. 0_o !");
        Assert.notNull(remoteReport.scenarioContext, "ScenarioContext is null after delegation. 0_o !");
        Assert.notNull(remoteReport.stepResults, "StepResults are null after delegation. 0_o !");
        Assert.notNull(remoteReport.status, "Status is null after delegation. 0_o !");
        Assert.notNull(remoteReport.information, "Information are null after delegation. 0_o !");
        Assert.notNull(remoteReport.errors, "Errors are null after delegation. 0_o !");
    }

}

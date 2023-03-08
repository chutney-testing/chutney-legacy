package com.chutneytesting.engine.domain.delegation;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
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
            StepExecutionReport remoteReport = delegationClient.handDown(step.definition(), agentInfo);

            guardFromIllegalReport(remoteReport);

            step.updateFrom(remoteReport.actionStatus, remoteReport.stepResults, remoteReport.scenarioContext, remoteReport.errors, remoteReport.information);

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

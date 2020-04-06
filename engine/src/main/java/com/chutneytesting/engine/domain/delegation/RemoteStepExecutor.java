package com.chutneytesting.engine.domain.delegation;

import com.google.common.collect.Lists;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

public class RemoteStepExecutor implements StepExecutor {

    private final DelegationClient delegationClient;
    private final NamedHostAndPort agentInfo;

    public RemoteStepExecutor(DelegationClient delegationClient, NamedHostAndPort agentInfo) {
        this.delegationClient = delegationClient;
        this.agentInfo = agentInfo;
    }

    @Override
    public void execute(ScenarioExecution scenarioExecution, StepContext localStepContext, TargetImpl target, Step step) {
        try {
            StepExecutionReport remoteReport = delegationClient.handDown(step.definition(), agentInfo);

            guardFromIllegalReport(remoteReport);

            updateLocalContext(step, localStepContext, remoteReport);
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

    private void updateLocalContext(Step step, StepContext localStepContext, StepExecutionReport remoteReport) {
        updateWith(localStepContext, remoteReport.scenarioContext, remoteReport.stepResults);
        propagateInformation(step, remoteReport.information);
        propagateStatus(step, remoteReport.status, remoteReport.errors);
    }

    private void propagateInformation(Step step, List<String> information) {
        step.addInformation((String[]) Lists.newArrayList(information).toArray(new String[information.size()]));
    }

    private void propagateStatus(Step step, Status reportStatus, List<String> errors) {
        if (reportStatus.equals(Status.FAILURE)) {
            step.failure((String[]) Lists.newArrayList(errors).toArray(new String[errors.size()]));
        } else {
            step.success();
        }
    }

    private void updateWith(StepContext localStepContext, Map<String, Object> scenarioContext, Map<String, Object> stepResults) {
        if(scenarioContext != null) {
            localStepContext.addScenarioContext(scenarioContext);
        }
        if(stepResults != null) {
            localStepContext.addStepOutputs(stepResults);
        }
    }

}

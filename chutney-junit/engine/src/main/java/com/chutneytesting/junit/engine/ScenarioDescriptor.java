package com.chutneytesting.junit.engine;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class ScenarioDescriptor extends AbstractTestDescriptor implements Node<ChutneyEngineExecutionContext> {

    private StepDefinition stepDefinition;
    private StepExecutionReport report;

    protected ScenarioDescriptor(UniqueId uniqueId, String displayName, TestSource source, StepDefinition stepDefinition) {
        super(uniqueId, displayName, source);
        this.stepDefinition = stepDefinition;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public ChutneyEngineExecutionContext execute(ChutneyEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        report = context.executeScenario(stepDefinition).blockingLast();
        return context;
    }

    @Override
    public void after(ChutneyEngineExecutionContext context) throws Exception {
        FeatureDescriptor featureDescriptor = (FeatureDescriptor) this.getParent().get();
        featureDescriptor.addScenarioStatus(report.status);

        if (Status.FAILURE.equals(report.status)) {
            StepExecutionReport failedStepReport = findFailedStep(report);
            throw new Exception(failedStepReport.errors.stream().reduce((s, s2) -> String.join("\n", s, s2))
                .orElse("Scenario " + stepDefinition.name + " FAILURE")
            );
        }
    }

    private StepExecutionReport findFailedStep(StepExecutionReport rootReport) {
        if (rootReport.steps.isEmpty()) {
            return rootReport;
        }

        StepExecutionReport failedChild = rootReport.steps.stream()
            .filter(r -> Status.FAILURE.equals(r.status))
            .findAny().get();
        return findFailedStep(failedChild);
    }
}

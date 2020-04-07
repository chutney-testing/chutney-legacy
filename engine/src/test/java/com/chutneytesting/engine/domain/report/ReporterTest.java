package com.chutneytesting.engine.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.RxBus;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.event.EndScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.event.StartScenarioExecutionEvent;
import com.chutneytesting.engine.domain.execution.report.Status;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import com.chutneytesting.task.spi.injectable.Target;
import io.reactivex.observers.TestObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class ReporterTest {

    private Target fakeTarget = TargetImpl.NONE;
    private StepDataEvaluator dataEvaluator = new StepDataEvaluator(new SpelFunctions());

    private Reporter sut;
    private Step step;
    private ScenarioExecution scenarioExecution;

    @Before
    public void before() {
        step = buildFakeScenario();
        sut = new Reporter();
        scenarioExecution = ScenarioExecution.createScenarioExecution();
    }

    @Test
    public void parent_status_should_be_recalculate() {
        Step subStep1 = step.subSteps().get(0);
        Step subSubStep1 = step.subSteps().get(0).subSteps().get(0);
        Step subSubStep2 = step.subSteps().get(0).subSteps().get(1);

        StepExecutionReport report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(0).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);

        step.beginExecution(scenarioExecution);
        subStep1.beginExecution(scenarioExecution);
        subSubStep1.beginExecution(scenarioExecution);
        report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);

        subSubStep1.pauseExecution(scenarioExecution);
        report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.PAUSED);
        assertThat(report.steps.get(0).status).isEqualTo(Status.PAUSED);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.PAUSED);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);

        subSubStep1.success();
        report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);

        subSubStep2.beginExecution(scenarioExecution);
        report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);

        subSubStep2.success();
        subStep1.endExecution(scenarioExecution);
        report = sut.generateReport(step);
        assertThat(report.status).isEqualTo(Status.RUNNING);
        assertThat(report.steps.get(0).status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps.get(0).steps.get(0).status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps.get(0).steps.get(1).status).isEqualTo(Status.SUCCESS);
        assertThat(report.steps.get(1).status).isEqualTo(Status.NOT_EXECUTED);
    }

    @Test
    public void should_publish_report_when_scenario_execution_notifications() {
        sut.createPublisher(scenarioExecution.executionId, mock(Step.class));
        TestObserver<StepExecutionReport> scenarioExecutionReportObservable = sut.subscribeOnExecution(scenarioExecution.executionId).test();
        scenarioExecutionReportObservable.assertValueCount(0);

        executeFakeScenarioSuccess();
        scenarioExecutionReportObservable.assertValueCount(10);

        scenarioExecutionReportObservable.dispose();
    }

    @Test
    public void should_retain_report_for_late_subscription() throws InterruptedException {
        sut.setRetentionDelaySeconds(1);
        sut.createPublisher(scenarioExecution.executionId, mock(Step.class));
        executeFakeScenarioSuccess();

        Thread.sleep(500);

        TestObserver<StepExecutionReport> scenarioExecutionReportObservable =
            sut.subscribeOnExecution(scenarioExecution.executionId).test();

        scenarioExecutionReportObservable.assertComplete();
        scenarioExecutionReportObservable.assertValueCount(1);

        scenarioExecutionReportObservable.dispose();
    }

    @Test
    public void should_return_empty_observable_for_unknown_execution_id() {
        TestObserver<StepExecutionReport> scenarioExecutionReportObservable =
            sut.subscribeOnExecution(0L).test();

        scenarioExecutionReportObservable.assertTerminated();
        scenarioExecutionReportObservable.assertValueCount(0);

        scenarioExecutionReportObservable.dispose();
    }

    private Step buildFakeScenario() {
        List<StepDefinition> subSubSteps = new ArrayList<>();
        StepDefinition subSubStepDef1 = new StepDefinition("fakeStep1", fakeTarget, "taskType", null, null, null, null);
        StepDefinition subSubStepDef2 = new StepDefinition("fakeStep2", fakeTarget, "taskType", null, null, null, null);
        subSubSteps.add(subSubStepDef1);
        subSubSteps.add(subSubStepDef2);
        StepDefinition subStepDef1 = new StepDefinition("fakeParentStep", fakeTarget, "taskType", null, null, subSubSteps, null);
        StepDefinition subStepDef2 = new StepDefinition("fakeParentEmptyStep", fakeTarget, "taskType", null, null, null, null);
        List<StepDefinition> steps = new ArrayList<>();
        steps.add(subStepDef1);
        steps.add(subStepDef2);
        StepDefinition rootStepDefinition = new StepDefinition("fakeScenario", fakeTarget, "taskType", null, null, steps, null);

        return buildStep(rootStepDefinition);
    }

    private Step buildStep(StepDefinition definition) {
        final List<Step> steps = Collections.unmodifiableList(definition.steps.stream().map(this::buildStep).collect(Collectors.toList()));
        return new Step(dataEvaluator, definition, Optional.empty(), null, steps);
    }

    private void executeFakeScenarioSuccess() {
        Step subStep1 = step.subSteps().get(0);
        Step subSubStep1 = step.subSteps().get(0).subSteps().get(0);
        Step subSubStep2 = step.subSteps().get(0).subSteps().get(1);

        RxBus.getInstance().post(new StartScenarioExecutionEvent(scenarioExecution, step));
        step.beginExecution(scenarioExecution);
        subStep1.beginExecution(scenarioExecution);
        subSubStep1.beginExecution(scenarioExecution);
        subSubStep1.success();
        subSubStep1.endExecution(scenarioExecution);
        subSubStep2.beginExecution(scenarioExecution);
        subSubStep2.success();
        subSubStep2.endExecution(scenarioExecution);
        subStep1.endExecution(scenarioExecution);
        step.endExecution(scenarioExecution);
        RxBus.getInstance().post(new EndScenarioExecutionEvent(scenarioExecution, step));
    }
}

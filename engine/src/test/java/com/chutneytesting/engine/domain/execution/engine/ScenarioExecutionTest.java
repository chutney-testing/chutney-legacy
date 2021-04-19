package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.RxBus.getInstance;
import static com.chutneytesting.tools.WaitUtils.awaitDuring;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.TestTaskTemplateLoader;
import com.chutneytesting.engine.domain.execution.action.PauseExecutionAction;
import com.chutneytesting.engine.domain.execution.action.ResumeExecutionAction;
import com.chutneytesting.engine.domain.execution.action.StopExecutionAction;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContextImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.evaluation.SpelFunctions;
import com.chutneytesting.engine.domain.execution.event.BeginStepExecutionEvent;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.FinallyAction.Builder;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ScenarioExecutionTest {

    @Test
    public void events_should_change_execution_state() {

        // Init
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution();
        assertThat(scenarioExecution.hasToPause()).isFalse();
        assertThat(scenarioExecution.hasToStop()).isFalse();

        // Pause
        getInstance().post(new PauseExecutionAction(scenarioExecution.executionId));
        assertThat(scenarioExecution.hasToPause()).isTrue();
        assertThat(scenarioExecution.hasToStop()).isFalse();

        // Resume
        getInstance().post(new ResumeExecutionAction(scenarioExecution.executionId));
        assertThat(scenarioExecution.hasToPause()).isFalse();
        assertThat(scenarioExecution.hasToStop()).isFalse();

        // Stop
        getInstance().post(new StopExecutionAction(scenarioExecution.executionId));
        assertThat(scenarioExecution.hasToPause()).isFalse();
        assertThat(scenarioExecution.hasToStop()).isTrue();

    }

    @Test
    public void finally_actions_are_executed_in_reverse_order() {
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution();

        TaskTemplateRegistry taskTemplateRegistry = TestTaskTemplateLoader.buildRegistry();

        HashMap<Object, Object> entries1 = Maps.newHashMap();
        entries1.put("key1", "value1");
        HashMap<Object, Object> entries2 = Maps.newHashMap();
        entries2.put("key2", "value2");
        FinallyAction first = Builder.forAction("context-put", "first").withInput("entries", entries1).build();
        FinallyAction second = Builder.forAction("failure", "second").build();
        FinallyAction third = Builder.forAction("context-put", "third").withInput("entries", entries2).build();
        scenarioExecution.registerFinallyAction(first);
        scenarioExecution.registerFinallyAction(second);
        scenarioExecution.registerFinallyAction(third);

        List<BeginStepExecutionEvent> events = new ArrayList<>();

        getInstance().register(BeginStepExecutionEvent.class, events::add);

        Step rootStep = createRootStep();
        scenarioExecution.executeFinallyActions(rootStep, new ScenarioContextImpl(), fa -> new Step(
            new StepDataEvaluator(new SpelFunctions()),
            new FinallyActionMapper().toStepDefinition(fa),
            Optional.empty(),
            new DefaultStepExecutor(taskTemplateRegistry),
            Collections.emptyList()
        ));

        awaitDuring(100, MILLISECONDS);

        //Test order is reversed
        Step thirdStep = events.get(0).step;
        Step secondStep = events.get(1).step;
        Step firstStep = events.get(2).step;

        assertThat(thirdStep.type()).isEqualTo("context-put");
        Map<String, Object> e1 = (Map<String, Object>) thirdStep.definition().inputs.get("entries");
        assertThat(e1).containsOnly(entry("key2", "value2"));

        assertThat(secondStep.type()).isEqualTo("failure");

        assertThat(firstStep.type()).isEqualTo("context-put");
        Map<String, Object> e2 = (Map<String, Object>) firstStep.definition().inputs.get("entries");
        assertThat(e2).containsOnly(entry("key1", "value1"));

    }

    @Test
    public void should_execute_finally_actions_on_scenario_stop() {
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution();

        TaskTemplateRegistry taskTemplateRegistry = TestTaskTemplateLoader.buildRegistry();

        FinallyAction finallyAction = Builder.forAction("final", "name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().register(BeginStepExecutionEvent.class, events::add);

        Step rootStep = createRootStep();

        // When
        getInstance().post(new StopExecutionAction(scenarioExecution.executionId));
        scenarioExecution.executeFinallyActions(rootStep, new ScenarioContextImpl(), fa -> new Step(
            new StepDataEvaluator(new SpelFunctions()),
            new FinallyActionMapper().toStepDefinition(fa),
            Optional.empty(),
            new DefaultStepExecutor(taskTemplateRegistry),
            Collections.emptyList()
        ));

        // Then
        Step finalStep = events.get(0).step;
        assertThat(finalStep.type()).isEqualTo("final");
    }

    @Test
    public void should_add_finally_actions_to_root_step() {
        ScenarioExecution scenarioExecution = ScenarioExecution.createScenarioExecution();

        TaskTemplateRegistry taskTemplateRegistry = TestTaskTemplateLoader.buildRegistry();

        FinallyAction finallyAction = Builder.forAction("final", "name").build();
        scenarioExecution.registerFinallyAction(finallyAction);

        List<BeginStepExecutionEvent> events = new ArrayList<>();
        getInstance().register(BeginStepExecutionEvent.class, events::add);

        Step rootStep = createRootStep();

        // When
        getInstance().post(new StopExecutionAction(scenarioExecution.executionId));
        scenarioExecution.executeFinallyActions(rootStep, new ScenarioContextImpl(), fa -> new Step(
            new StepDataEvaluator(new SpelFunctions()),
            new FinallyActionMapper().toStepDefinition(fa),
            Optional.empty(),
            new DefaultStepExecutor(taskTemplateRegistry),
            Collections.emptyList()
        ));

        // Then
        assertThat(rootStep.subSteps().size()).isEqualTo(1);
        assertThat(rootStep.subSteps().get(0).definition().name).isEqualTo("Finally action generated for name");
    }

    private Step createRootStep() {
        StepDefinition stepDefinition = new StepDefinition("rootStep", null, "type", null, null, null, null, null, null);
        return new Step(null, stepDefinition, Optional.empty(), null, newArrayList());
    }

}

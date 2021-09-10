package com.chutneytesting.task.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FinalTaskTest {

    private FinalTask sut;

    @Test
    void task_identifier_is_mandatory() {
        assertThrows(
            NullPointerException.class,
            () -> new FinalTask(new TestLogger(), null, null, null, null, null, null, null)
        );
    }

    @Test
    void should_register_simple_finally_action() {
        TestLogger logger = new TestLogger();
        List<FinallyAction> registeredFinallyActions = new ArrayList<>();
        sut = new FinalTask(logger, registeredFinallyActions::add, "debug", null, null, null, null, null);

        TaskExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(result.outputs).isEmpty();

        assertThat(logger.info).containsExactly("debug as finally action registered");
        assertThat(logger.errors).isEmpty();

        assertThat(registeredFinallyActions).hasSize(1);
        FinallyAction finallyAction = registeredFinallyActions.get(0);
        assertThat(finallyAction.originalTask()).isEqualTo(FinalTask.class.getSimpleName());
        assertThat(finallyAction.actionIdentifier()).isEqualTo("debug");
        assertThat(finallyAction.target()).isEmpty();
        assertThat(finallyAction.inputs()).isEmpty();
        assertThat(finallyAction.strategyType()).isEmpty();
        assertThat(finallyAction.strategyProperties()).isEmpty();
    }

    @Test
    void should_register_task_with_inputs_and_strategy_finally_action() {
        TestLogger logger = new TestLogger();
        List<FinallyAction> registeredFinallyActions = new ArrayList<>();
        Target target = TestTarget.TestTargetBuilder.builder().withTargetId("target name").withUrl("url").build();

        Map<String, Object> inputs = Map.of("k", "v", "kk", new Object());
        String strategyType = "retry";
        Map<String, Object> strategyProperties = Map.of("timeout", "1 s", "delay", new Object());
        sut = new FinalTask(logger, registeredFinallyActions::add, "complex task", "testing ??", target, inputs, strategyType, strategyProperties);

        sut.execute();

        assertThat(registeredFinallyActions).hasSize(1);
        FinallyAction finallyAction = registeredFinallyActions.get(0);
        assertThat(finallyAction.originalTask()).isEqualTo("testing ??");
        assertThat(finallyAction.actionIdentifier()).isEqualTo("complex task");
        assertThat(finallyAction.target()).contains(target);
        assertThat(finallyAction.inputs()).containsAllEntriesOf(inputs);
        assertThat(finallyAction.strategyType()).contains(strategyType);
        assertThat(finallyAction.strategyProperties()).containsSame(strategyProperties);
    }
}

package com.chutneytesting.engine.domain.execution.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.Test;

import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.TargetSpiImpl;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.injectable.Target;

public class FinallyActionMapperTest {

    private final FinallyActionMapper mapper = new FinallyActionMapper();

    @Test
    public void upright_finally_action_copy() {
        ImmutableTarget domainTarget = ImmutableTarget.builder()
            .id(com.chutneytesting.engine.domain.environment.Target.TargetId.of("test-target"))
            .url("proto://host:12345")
            .build();
        Target taskTarget = new TargetSpiImpl(domainTarget);
        FinallyAction finallyAction = FinallyAction.Builder
            .forAction("test-action")
            .withTarget(taskTarget)
            .withInput("test-input", "test")
            .build();

        StepDefinition stepDefinition = mapper.toStepDefinition(finallyAction);

        assertThat(stepDefinition.type).isEqualTo("test-action");
        assertThat(stepDefinition.inputs).containsOnly(entry("test-input", "test"));
        assertThat(stepDefinition.getTarget()).isPresent();
        com.chutneytesting.engine.domain.environment.Target targetCopy = stepDefinition.getTarget().get();
        assertThat(targetCopy.name()).isEqualTo("test-target");
        assertThat(targetCopy.url()).isEqualTo("proto://host:12345");
        assertThat(targetCopy.security().credential()).isEmpty();
    }
}

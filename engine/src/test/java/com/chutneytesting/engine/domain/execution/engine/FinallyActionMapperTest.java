package com.chutneytesting.engine.domain.execution.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.engine.parameterResolver.TargetSpiImpl;
import com.chutneytesting.task.spi.FinallyAction;
import org.junit.Test;

public class FinallyActionMapperTest {

    private final FinallyActionMapper mapper = new FinallyActionMapper();

    @Test
    public void upright_finally_action_copy() {
        Target domainTarget = Target.builder()
            .withName("test-target")
            .withUrl("proto://host:12345")
            .build();
        com.chutneytesting.task.spi.injectable.Target taskTarget = new TargetSpiImpl(domainTarget);
        FinallyAction finallyAction = FinallyAction.Builder
            .forAction("test-action")
            .withTarget(taskTarget)
            .withInput("test-input", "test")
            .build();

        StepDefinition stepDefinition = mapper.toStepDefinition(finallyAction);

        assertThat(stepDefinition.type).isEqualTo("test-action");
        assertThat(stepDefinition.inputs).containsOnly(entry("test-input", "test"));
        assertThat(stepDefinition.getTarget()).isPresent();
        Target targetCopy = stepDefinition.getTarget().get();
        assertThat(targetCopy.name()).isEqualTo("test-target");
        assertThat(targetCopy.url).isEqualTo("proto://host:12345");
        assertThat(targetCopy.security.credential()).isEmpty();
    }
}

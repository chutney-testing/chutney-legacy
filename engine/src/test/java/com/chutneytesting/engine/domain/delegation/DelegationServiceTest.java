package com.chutneytesting.engine.domain.delegation;

import static com.chutneytesting.engine.domain.environment.ImmutableTarget.copyOf;
import static com.chutneytesting.engine.domain.environment.SecurityInfo.SecurityInfoBuilder;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.environment.Target.TargetId;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DelegationServiceTest {

    public static Object[] parametersForShould_return_local_executor() {

        Target targetWithNoName = createTarget();
        Target targetWithNoAgents = copyOf(createTarget())
            .withId(TargetId.of("name"));
        Target targetWithEmptyAgentsList = copyOf(createTarget())
            .withId(TargetId.of("name"))
            .withAgents(emptyList());
        return new Object[][]{
            {empty()},
            {of(targetWithNoName)},
            {of(targetWithNoAgents)},
            {of(targetWithEmptyAgentsList)},
        };
    }

    @Test
    @Parameters
    public void should_return_local_executor(Optional<Target> target) {
        // Given
        DelegationService delegationService = new DelegationService(mock(DefaultStepExecutor.class), mock(DelegationClient.class));

        // When
        StepExecutor actual = delegationService.findExecutor(target);

        // Then
        assertThat(actual).as("StepExecutor").isInstanceOf(DefaultStepExecutor.class);
    }

    @Test
    public void should_return_remote_executor() {
        // Given
        DelegationService delegationService = new DelegationService(mock(DefaultStepExecutor.class), mock(DelegationClient.class));
        Target targetWithAgent = copyOf(createTarget())
            .withId(TargetId.of("name"))
            .withAgents(Lists.newArrayList(new NamedHostAndPort("name", "host", 12345)));

        // When
        StepExecutor actual = delegationService.findExecutor(of(targetWithAgent));

        // Then
        assertThat(actual).as("StepExecutor").isInstanceOf(RemoteStepExecutor.class);
        assertThat(targetWithAgent.agents().get()).isEmpty();
    }

    private static Target createTarget() {
        return ImmutableTarget.builder()
            .id(Target.TargetId.of(""))
            .url("proto://host:12345")
            .build();
    }


}

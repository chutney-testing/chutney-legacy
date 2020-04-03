package com.chutneytesting.engine.domain.delegation;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.engine.DefaultStepExecutor;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.google.common.collect.Lists;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class DelegationServiceTest {

    public static Object[] parametersForShould_return_local_executor() {
        Target targetWithNoName = Target.builder()
            .withUrl("proto://host:12345")
            .build();
        Target targetWithNoAgents = Target.builder().copyOf(targetWithNoName)
            .withName("name")
            .build();
        return new Object[][]{
            {empty()},
            {of(targetWithNoName)},
            {of(targetWithNoAgents)},
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
        Target targetWithAgent = Target.builder()
            .withName("name")
            .withAgents(Lists.newArrayList(new NamedHostAndPort("name", "host", 12345)))
            .build();

        // When
        StepExecutor actual = delegationService.findExecutor(of(targetWithAgent));

        // Then
        assertThat(actual).as("StepExecutor").isInstanceOf(RemoteStepExecutor.class);
        assertThat(targetWithAgent.agents).isEmpty();
    }

}

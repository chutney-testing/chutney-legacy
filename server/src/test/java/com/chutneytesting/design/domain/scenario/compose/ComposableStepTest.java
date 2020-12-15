package com.chutneytesting.design.domain.scenario.compose;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ComposableStepTest {

    @Test
    public void should_throw_when_cyclic_dependency_found() {
        // Given
        ComposableStep himself = ComposableStep.builder().withName("himself").build();

        // When
        try {
            ComposableStep.builder()
                .from(himself)
                .withSteps(singletonList(himself))
                .build();
        } catch (Exception e) {
            // Then
            assertThat(e).isInstanceOf(ComposableStepCyclicDependencyException.class);
            assertThat(e.getMessage()).contains(himself.name);
            return;
        }
        Assertions.fail("Should throw when a cyclic dependency is found !");
    }

    @Test
    public void should_not_found_cyclic_dependency() {
        // Given
        ComposableStep leaf_1   = ComposableStep.builder().withId("leaf 1").build();
        ComposableStep leaf_2   = ComposableStep.builder().withId("leaf 2").build();
        ComposableStep subStep    = ComposableStep.builder().withId("subStep")
            .withSteps(asList(leaf_1, leaf_2))
            .build();
        ComposableStep parent    = ComposableStep.builder().withId("parent")
            .withSteps(singletonList(subStep))
            .build();

        // When
        ComposableStep.builder().withId("scenario")
            .withSteps(asList(subStep, parent, subStep))
            .build();
    }



}

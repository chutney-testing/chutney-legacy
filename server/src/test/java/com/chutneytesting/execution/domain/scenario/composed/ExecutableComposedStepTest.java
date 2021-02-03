package com.chutneytesting.execution.domain.scenario.composed;


import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ExecutableComposedStepTest {

    @Test
    public void should_deal_with_childs_rise_same_dataset_key() {
        // Given
        ExecutableComposedStep step_1 = getStepWithEntry("emptyValue", "");
        ExecutableComposedStep step_2 = getStepWithEntry("emptyValue", "");
        ExecutableComposedStep step_3 = getStepWithEntry("", "");
        ExecutableComposedStep step_4 = getStepWithEntry("", "");
        ExecutableComposedStep step_5 = getStepWithEntry("withNotEmptyValue", "value");

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withSteps(newArrayList(step_1, step_2, step_3, step_4, step_5))
            .build();

        assertThat(step.executionParameters).hasSize(2);
        assertThat(step.executionParameters).containsEntry("emptyValue", "");
        assertThat(step.executionParameters).containsEntry("", "");
    }

    private ExecutableComposedStep getStepWithEntry(String key, String value) {
        return ExecutableComposedStep.builder()
            .withExecutionParameters(singletonMap(key, value))
            .build();
    }
}

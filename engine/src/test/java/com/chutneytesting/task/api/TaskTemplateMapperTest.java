package com.chutneytesting.task.api;

import static com.chutneytesting.task.TestTaskTemplateHelper.mockParameter;
import static com.chutneytesting.task.TestTaskTemplateHelper.mockTaskTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.task.api.TaskDto.InputsDto;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class TaskTemplateMapperTest {

    @Test
    public void should_map_task_template_with_no_inputs_parameters() {
        // Given
        final String TASK_ID = "task-id";
        TaskTemplate taskTemplate = mockTaskTemplate(TASK_ID, new HashSet<>(Arrays.asList(mockParameter(String.class), mockParameter(String.class))));

        // When
        TaskDto taskDto = TaskTemplateMapper.toDto(taskTemplate);

        // Then
        assertThat(taskDto.getIdentifier()).isEqualTo(TASK_ID);
        assertThat(taskDto.getInputs()).isEmpty();
        assertThat(taskDto.target()).isFalse();
    }

    @Test
    public void should_map_task_template_with_inputs_parameters() {
        // Given
        final String TASK_ID = "task-id";
        final Class<?> INPUT_TYPE_1 = String.class;
        final String INPUT_NAME_2 = "inputName2";
        final Class<?> INPUT_TYPE_2 = String.class;
        final String INPUT_NAME_3 = "inputName3";
        final Class<?> INPUT_TYPE_3 = Object.class;

        Parameter targetParameter = mock(Parameter.class, RETURNS_DEEP_STUBS);
        Class targetClass = Target.class;
        when(targetParameter.rawType()).thenReturn(targetClass);

        TaskTemplate taskTemplate = mockTaskTemplate(TASK_ID, new HashSet<>(Arrays.asList(mockParameter(INPUT_TYPE_1), mockParameter(INPUT_TYPE_2, INPUT_NAME_2), mockParameter(INPUT_TYPE_3, INPUT_NAME_3), targetParameter)));

        // When
        TaskDto taskDto = TaskTemplateMapper.toDto(taskTemplate);

        // Then
        assertThat(taskDto.getIdentifier()).isEqualTo(TASK_ID);
        assertThat(taskDto.target()).isTrue();
        assertThat(taskDto.getInputs()).containsExactlyInAnyOrder(new InputsDto(INPUT_NAME_2, INPUT_TYPE_2), new InputsDto(INPUT_NAME_3, INPUT_TYPE_3));
    }

    @Test
    public void should_map_task_template_with_complex_inputs_parameters() {
        // Given
        final String TASK_ID = "task-id";
        final String INPUT_NAME_1 = "complexParam";
        final Class<?> INPUT_TYPE_1 = ComplexParameterTestClass.class;
        final String INPUT_NAME_2 = "complexParam2";
        final Class<?> INPUT_TYPE_2 = Integer.class;
        TaskTemplate taskTemplate = mockTaskTemplate(TASK_ID, new HashSet<>(Arrays.asList(mockParameter(INPUT_TYPE_1, INPUT_NAME_1), mockParameter(INPUT_TYPE_2, INPUT_NAME_2))));

        // When
        TaskDto taskDto = TaskTemplateMapper.toDto(taskTemplate);

        // Then
        assertThat(taskDto.getIdentifier()).isEqualTo(TASK_ID);
        assertThat(taskDto.target()).isFalse();
        assertThat(taskDto.getInputs())
            .containsExactlyInAnyOrder(
                new InputsDto("first", String.class),
                new InputsDto("second", Integer.class),
                new InputsDto("complexParam2", Integer.class)
            );
    }
}

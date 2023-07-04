package com.chutneytesting.action.api;

import static com.chutneytesting.action.TestActionTemplateHelper.mockActionTemplate;
import static com.chutneytesting.action.TestActionTemplateHelper.mockParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.api.ActionDto.InputsDto;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.parameter.Parameter;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class ActionTemplateMapperTest {

    @Test
    public void should_map_action_template_with_no_inputs_parameters() {
        // Given
        final String TASK_ID = "action-id";
        ActionTemplate actionTemplate = mockActionTemplate(TASK_ID, new HashSet<>(Arrays.asList(mockParameter(String.class), mockParameter(String.class))));

        // When
        ActionDto actionDto = ActionTemplateMapper.toDto(actionTemplate);

        // Then
        assertThat(actionDto.getIdentifier()).isEqualTo(TASK_ID);
        assertThat(actionDto.getInputs()).isEmpty();
        assertThat(actionDto.target()).isFalse();
    }

    @Test
    public void should_map_action_template_with_inputs_parameters() {
        // Given
        final String TASK_ID = "action-id";
        final Class<?> INPUT_TYPE_1 = String.class;
        final String INPUT_NAME_2 = "inputName2";
        final Class<?> INPUT_TYPE_2 = String.class;
        final String INPUT_NAME_3 = "inputName3";
        final Class<?> INPUT_TYPE_3 = Object.class;

        Parameter targetParameter = mock(Parameter.class, RETURNS_DEEP_STUBS);
        Class targetClass = Target.class;
        when(targetParameter.rawType()).thenReturn(targetClass);

        ActionTemplate actionTemplate = mockActionTemplate(TASK_ID, new HashSet<>(Arrays.asList(mockParameter(INPUT_TYPE_1), mockParameter(INPUT_TYPE_2, INPUT_NAME_2), mockParameter(INPUT_TYPE_3, INPUT_NAME_3), targetParameter)));

        // When
        ActionDto actionDto = ActionTemplateMapper.toDto(actionTemplate);

        // Then
        assertThat(actionDto.getIdentifier()).isEqualTo(TASK_ID);
        assertThat(actionDto.target()).isTrue();
        assertThat(actionDto.getInputs()).containsExactlyInAnyOrder(new InputsDto(INPUT_NAME_2, INPUT_TYPE_2), new InputsDto(INPUT_NAME_3, INPUT_TYPE_3));
    }

}

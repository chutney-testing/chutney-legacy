/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.action.spi.ActionExecutionResult.ok;
import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.TestActionTemplateFactory.ComplexAction;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateParserV2;
import com.chutneytesting.action.domain.ActionTemplateRegistry;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DefaultStepExecutorTest {

    @Test
    public void should_execute_the_fake_action() {
        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        ActionTemplate actionTemplate = mock(ActionTemplate.class, RETURNS_DEEP_STUBS);
        when(actionTemplate.create(any()).validateInputs()).thenReturn(emptyList());
        when(actionTemplate.create(any()).execute()).thenReturn(ok());
        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(of(actionTemplate));
        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepExecutor stepExecutor = new DefaultStepExecutor(actionTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(null), mock(TargetImpl.class), step);

        verify(actionTemplate.create(any()), times(1)).execute();
        verify(step, times(0)).failure(any(Exception.class));
    }

    @Test
    public void should_fail_step_with_message_on_action_error() {
        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        ActionTemplate actionTemplate = mock(ActionTemplate.class, RETURNS_DEEP_STUBS);
        when(actionTemplate.create(any()).execute()).thenThrow(RuntimeException.class);
        when(actionTemplate.create(any()).validateInputs()).thenReturn(emptyList());
        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(of(actionTemplate));
        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepExecutor stepExecutor = new DefaultStepExecutor(actionTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(null), mock(TargetImpl.class), step);

        verify(step, times(1)).failure("Action [null] failed: java.lang.RuntimeException");
    }

    @Test
    public void should_fail_step_with_message_on_validation_error() {
        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        ActionTemplate actionTemplate = mock(ActionTemplate.class, RETURNS_DEEP_STUBS);
        when(actionTemplate.create(any()).validateInputs()).thenReturn(singletonList("validation error"));
        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(of(actionTemplate));
        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepExecutor stepExecutor = new DefaultStepExecutor(actionTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(null), mock(TargetImpl.class), step);

        verify(step, times(1)).failure("validation error");
    }

    @Test
    public void should_execute_a_real_action() {
        ActionTemplateRegistry actionTemplateRegistry = mock(ActionTemplateRegistry.class);
        ActionTemplate actionTemplate = new ActionTemplateParserV2().parse(ComplexAction.class).result();

        when(actionTemplateRegistry.getByIdentifier(any())).thenReturn(of(actionTemplate));

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("stringParam", "teststring");
        inputs.put("param1", "a");
        inputs.put("param2", "b");

        Step step = mock(Step.class, RETURNS_DEEP_STUBS);
        when(step.getEvaluatedInputs()).thenReturn(inputs);

        StepExecutor stepExecutor = new DefaultStepExecutor(actionTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(null), mock(TargetImpl.class), step);

        verify(step, times(0)).failure(any(Exception.class));
    }
}

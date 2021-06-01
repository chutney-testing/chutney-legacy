package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.execution.ScenarioExecution.createScenarioExecution;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;
import com.chutneytesting.task.TestTaskTemplateFactory.ComplexTask;
import com.chutneytesting.task.TestTaskTemplateFactory.OomTask;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateParserV2;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.spi.TaskExecutionResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class DefaultStepExecutorTest {

    @Test
    public void should_execute_the_fake_task() {
        TaskTemplateRegistry taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        TaskTemplate taskTemplate = mock(TaskTemplate.class, RETURNS_DEEP_STUBS);
        when(taskTemplate.create(any()).execute()).thenReturn(TaskExecutionResult.ok());
        when(taskTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.of(taskTemplate));
        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepContext stepContext = mock(StepContext.class);

        StepExecutor stepExecutor = new DefaultStepExecutor(taskTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(), stepContext, mock(TargetImpl.class), step);

        verify(taskTemplate.create(any()), times(1)).execute();
        verify(step, times(0)).failure(any(Exception.class));
    }

    @Test
    public void should_fail_step_with_message_on_task_error() {
        TaskTemplateRegistry taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        TaskTemplate taskTemplate = mock(TaskTemplate.class, RETURNS_DEEP_STUBS);
        when(taskTemplate.create(any()).execute()).thenThrow(RuntimeException.class);
        when(taskTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.of(taskTemplate));
        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepContext stepContext = mock(StepContext.class);

        StepExecutor stepExecutor = new DefaultStepExecutor(taskTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(), stepContext, mock(TargetImpl.class), step);

        verify(step, times(1)).failure("Task [null] failed: java.lang.RuntimeException");
    }

    @Test
    public void should_execute_a_real_task() {
        TaskTemplateRegistry taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(ComplexTask.class).result();

        when(taskTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.of(taskTemplate));

        StepContext stepContext = mock(StepContext.class);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("stringParam", "teststring");
        inputs.put("param1", "a");
        inputs.put("param2", "b");
        when(stepContext.getEvaluatedInputs()).thenReturn(inputs);

        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepExecutor stepExecutor = new DefaultStepExecutor(taskTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(), stepContext, mock(TargetImpl.class), step);

        verify(step, times(0)).failure(any(Exception.class));
    }

    @Test
    public void should_prevent_oom_exceptions() {
        TaskTemplateRegistry taskTemplateRegistry = mock(TaskTemplateRegistry.class);
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(OomTask.class).result();

        when(taskTemplateRegistry.getByIdentifier(any())).thenReturn(Optional.of(taskTemplate));

        StepContext stepContext = mock(StepContext.class);

        Step step = mock(Step.class, RETURNS_DEEP_STUBS);

        StepExecutor stepExecutor = new DefaultStepExecutor(taskTemplateRegistry);
        stepExecutor.execute(createScenarioExecution(), stepContext, mock(TargetImpl.class), step);

        verify(step, times(0)).failure(any(Exception.class));
    }
}

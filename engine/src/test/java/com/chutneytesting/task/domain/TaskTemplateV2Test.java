package com.chutneytesting.task.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.task.TestTaskTemplateFactory.ComplexeTask;
import com.chutneytesting.task.TestTaskTemplateFactory.Pojo;
import com.chutneytesting.task.TestTaskTemplateFactory.TwoParametersTask;
import com.chutneytesting.task.TestTaskTemplateFactory.ValidSimpleTask;
import com.chutneytesting.task.TypeBasedParameterResolver;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.TaskExecutionResult.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;

public class TaskTemplateV2Test {

    @Test
    public void simple_task_instantiation() {
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(ValidSimpleTask.class).result();

        Task task = taskTemplate.create(Collections.emptyList());

        assertThat(task).isExactlyInstanceOf(ValidSimpleTask.class);
    }

    @Test
    public void task_with_parameters_instantiation_and_execution() {
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(TwoParametersTask.class).result();
        String stringValue = UUID.randomUUID().toString();

        Task task = taskTemplate.create(Arrays.asList(
            new TypeBasedParameterResolver<>(String.class, p -> stringValue),
            new TypeBasedParameterResolver<>(int.class, p -> 0)
        ));

        assertThat(task).isExactlyInstanceOf(TwoParametersTask.class);

        TaskExecutionResult executionResult = task.execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("someString", stringValue), entry("someInt", 0));
    }

    @Test
    public void task_with_complexe_parameters_instantiation_and_execution() {
        TaskTemplate taskTemplate = new TaskTemplateParserV2().parse(ComplexeTask.class).result();
        String stringValue = UUID.randomUUID().toString();
        Pojo pojo = new Pojo("1", "2");
        Task task = taskTemplate.create(Arrays.asList(
            new TypeBasedParameterResolver<>(String.class, p -> stringValue),
            new TypeBasedParameterResolver<>(Pojo.class, p -> pojo)
        ));

        assertThat(task).isExactlyInstanceOf(ComplexeTask.class);

        TaskExecutionResult executionResult = task.execute();

        assertThat(executionResult.status).isEqualTo(Status.Success);
        assertThat(executionResult.outputs).containsOnly(entry("someString", stringValue), entry("someObject", pojo));
    }
}

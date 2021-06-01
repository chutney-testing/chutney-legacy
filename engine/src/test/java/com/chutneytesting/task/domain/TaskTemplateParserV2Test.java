package com.chutneytesting.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTaskTemplateFactory.ComplexTask;
import com.chutneytesting.task.TestTaskTemplateFactory.TwoConstructorTask;
import com.chutneytesting.task.TestTaskTemplateFactory.TwoParametersTask;
import com.chutneytesting.task.TestTaskTemplateFactory.ValidSimpleTask;
import org.junit.jupiter.api.Test;

public class TaskTemplateParserV2Test {

    private TaskTemplateParserV2 parser = new TaskTemplateParserV2();

    @Test
    public void simple_task_parsing() {
        ResultOrError<TaskTemplate, ParsingError> parsingResult = parser.parse(ValidSimpleTask.class);

        assertThat(parsingResult.isOk()).isTrue();
        TaskTemplate taskTemplate = parsingResult.result();
        assertThat(taskTemplate.identifier()).isEqualTo("valid-simple");
        assertThat(taskTemplate.implementationClass()).isEqualTo(ValidSimpleTask.class);
        assertThat(taskTemplate.parameters()).hasSize(0);
    }

    @Test
    public void complex_task_parsing() {
        ResultOrError<TaskTemplate, ParsingError> parsingResult = parser.parse(ComplexTask.class);

        assertThat(parsingResult.isOk()).isTrue();
        TaskTemplate taskTemplate = parsingResult.result();
        assertThat(taskTemplate.identifier()).isEqualTo("complex");
        assertThat(taskTemplate.implementationClass()).isEqualTo(ComplexTask.class);
        assertThat(taskTemplate.parameters()).hasSize(2);
    }


    @Test
    public void task_with_parameters_parsing() {
        ResultOrError<TaskTemplate, ParsingError> parsingResult = parser.parse(TwoParametersTask.class);

        assertThat(parsingResult.isOk()).isTrue();
        TaskTemplate taskTemplate = parsingResult.result();
        assertThat(taskTemplate.identifier()).isEqualTo("two-parameters");
        assertThat(taskTemplate.implementationClass()).isEqualTo(TwoParametersTask.class);
        assertThat(taskTemplate.parameters()).hasSize(2);
    }

    @Test
    public void task_with_more_than_one_constructor() {
        TaskTemplateParserV2 parser = new TaskTemplateParserV2();
        ResultOrError<TaskTemplate, ParsingError> parsingResult = parser.parse(TwoConstructorTask.class);
        assertThat(parsingResult.isError()).isTrue();
        assertThat(parsingResult.error().errorMessage()).isEqualTo("More than one constructor");
    }
}

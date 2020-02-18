package com.chutneytesting.task.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTaskTemplateFactory.TestTask;
import com.chutneytesting.task.TestTaskTemplateFactory.TestTask3;
import com.chutneytesting.task.domain.ParsingError;
import com.chutneytesting.task.domain.ResultOrError;
import com.chutneytesting.task.domain.TaskInstantiationFailureException;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateParser;
import com.chutneytesting.task.domain.UnresolvableTaskParameterException;
import com.chutneytesting.task.domain.parameter.Parameter;
import com.chutneytesting.task.domain.parameter.ParameterResolver;
import com.chutneytesting.task.spi.Task;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class DefaultTaskTemplateLoaderTest {

    @Test
    public void load_from_test_file() {
        DefaultTaskTemplateLoader<TestTask> taskTemplateLoader = new DefaultTaskTemplateLoader<>(
            "test.tasks",
            TestTask.class,
            new TestTaskTemplateParser());

        assertThat(taskTemplateLoader.load())
            .as("Loaded TaskTemplates")
            .hasSize(2)
            .extracting(TaskTemplate::identifier).containsExactlyInAnyOrder("TestTask1", "TestTask2");

    }

    static class TestTaskTemplateParser implements TaskTemplateParser<TestTask> {

        @Override
        public ResultOrError<TaskTemplate, ParsingError> parse(Class<? extends TestTask> taskClass) {
            if (TestTask3.class.equals(taskClass)) {
                return ResultOrError.error(new ParsingError(taskClass, "test error"));
            }
            TaskTemplate taskTemplate = new TaskTemplate() {

                @Override
                public String identifier() {
                    return taskClass.getSimpleName();
                }

                @Override
                public Class<?> implementationClass() {
                    return taskClass;
                }

                @Override
                public Set<Parameter> parameters() {
                    return Collections.emptySet();
                }

                @Override
                public Task create(List<ParameterResolver> parameterResolvers) throws UnresolvableTaskParameterException, TaskInstantiationFailureException {
                    throw new RuntimeException(TestTask.class.getSimpleName() + "s are not instantiable");
                }
            };
            return ResultOrError.result(taskTemplate);
        }
    }
}

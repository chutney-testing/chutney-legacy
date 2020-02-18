package com.chutneytesting.task.domain;

import static com.chutneytesting.task.TestTaskTemplateFactory.buildTaskTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTaskTemplateFactory.TestTask1;
import com.chutneytesting.task.TestTaskTemplateFactory.TestTask2;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class DefaultTaskTemplateRegistryTest {

    @Test
    public void getByType_returns_matching_taskTemplate() {
        String taskType = "test-type";
        DefaultTaskTemplateRegistry taskTemplateRegistry = withTasks(buildTaskTemplate(taskType, TestTask1.class));

        assertThat(taskTemplateRegistry.getByIdentifier(taskType)).isPresent();
    }

    @Test
    public void getByType_returns_empty_when_no_taskTemplate_matches() {
        String taskType = "test-type";
        DefaultTaskTemplateRegistry taskTemplateRegistry = withTasks(buildTaskTemplate(taskType, TestTask1.class));

        assertThat(taskTemplateRegistry.getByIdentifier("unknown")).isEmpty();
    }

    @Test
    public void registry_keep_the_first_taskTemplate_with_the_same_identifier() {
        String taskType = "test-type";
        TaskTemplate primaryTaskTemplate = buildTaskTemplate(taskType, TestTask1.class);
        DefaultTaskTemplateRegistry taskTemplateRegistry = withTasks(primaryTaskTemplate, buildTaskTemplate(taskType, TestTask2.class));

        assertThat(taskTemplateRegistry.getByIdentifier("test-type")).hasValue(primaryTaskTemplate);
    }

    private DefaultTaskTemplateRegistry withTasks(TaskTemplate... taskTemplates) {
        TaskTemplateLoader taskTemplateLoader = () -> Arrays.asList(taskTemplates);
        return new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(Collections.singletonList(taskTemplateLoader)));
    }
}

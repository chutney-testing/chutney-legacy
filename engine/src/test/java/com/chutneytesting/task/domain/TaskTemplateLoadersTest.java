package com.chutneytesting.task.domain;

import static com.chutneytesting.task.TestTaskTemplateFactory.buildTaskTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.TestTaskTemplateFactory.TestTask1;
import com.chutneytesting.task.TestTaskTemplateFactory.TestTask2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TaskTemplateLoadersTest {

    @Test
    public void load_tasks_in_order() {
        List<TaskTemplateLoader> loaders = new ArrayList<>();
        loaders.add(() -> Collections.singletonList(buildTaskTemplate("task1", TestTask1.class)));
        loaders.add(() -> Collections.singletonList(buildTaskTemplate("task2", TestTask2.class)));
        TaskTemplateLoaders taskTemplateLoaders = new TaskTemplateLoaders(loaders);

        assertThat(taskTemplateLoaders.orderedTemplates())
            .as("TaskTemplates from TaskTemplateLoaders")
            .hasSize(2)
            .extracting(TaskTemplate::identifier)
            .containsExactly("task1", "task2");
    }
}

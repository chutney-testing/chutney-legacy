package com.chutneytesting.task.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateParserV2;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EmbeddedTaskEngineTest {

    private EmbeddedTaskEngine engine ;
    private TaskTemplateParserV2 parser = new TaskTemplateParserV2();

    @BeforeEach
    public void setUp() {
        // G
        TaskTemplateRegistry regitry = Mockito.mock(TaskTemplateRegistry.class);
        List<TaskTemplate> tasks = Lists.newArrayList();
        tasks.add(parser.parse(TestTask.class).result());
        tasks.add(parser.parse(Test2Task.class).result());

        Mockito.when(regitry.getAll()).thenReturn(tasks);

        this.engine = new EmbeddedTaskEngine(regitry);
    }

    @Test
    public void getAllTasks() {
        // W
        List<TaskDto> allTasks = engine.getAllTasks();

        // T
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks.get(0).getIdentifier()).isEqualTo("test");
        assertThat(allTasks.get(1).getIdentifier()).isEqualTo("test2");
    }

    @Test
    public void getTask() {
        // W
        Optional<TaskDto> task = engine.getTask("test");

        // T
        assertThat(task).isPresent();
        assertThat(task.get().getIdentifier()).isEqualTo("test");
    }


    private static class TestTask implements Task {
        public TaskExecutionResult execute() {
            return TaskExecutionResult.ok();
        }
    }

    private static class Test2Task implements Task {
        public TaskExecutionResult execute() {
            return TaskExecutionResult.ok();
        }
    }
}

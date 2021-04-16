package com.chutneytesting.engine.domain.execution;

import com.chutneytesting.task.TestTaskTemplateFactory.FailTask;
import com.chutneytesting.task.TestTaskTemplateFactory.SuccessTask;
import com.chutneytesting.task.domain.DefaultTaskTemplateRegistry;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateLoader;
import com.chutneytesting.task.domain.TaskTemplateLoaders;
import com.chutneytesting.task.domain.TaskTemplateParserV2;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Minimal {@link TaskTemplateLoader} with simple tasks:
 * <ul>
 * <li>{@link SuccessTask}</li>
 * <li>{@link FailTask}</li>
 * </ul>
 */
public class TestTaskTemplateLoader implements TaskTemplateLoader {

    private final List<TaskTemplate> taskTemplates = new ArrayList<>();

    public TestTaskTemplateLoader() {
        this.taskTemplates.add(new TaskTemplateParserV2().parse(SuccessTask.class).result());
        this.taskTemplates.add(new TaskTemplateParserV2().parse(FailTask.class).result());
    }

    @Override
    public List<TaskTemplate> load() {
        return taskTemplates;
    }

    public static TaskTemplateRegistry buildRegistry() {
        return new DefaultTaskTemplateRegistry(new TaskTemplateLoaders(Collections.singletonList(new TestTaskTemplateLoader())));
    }
}

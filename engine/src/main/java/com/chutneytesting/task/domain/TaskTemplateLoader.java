package com.chutneytesting.task.domain;

import java.util.List;

/**
 * Simple loader of {@link TaskTemplate}.
 *
 * @see DefaultTaskTemplateRegistry
 */
@FunctionalInterface
public interface TaskTemplateLoader {

    List<TaskTemplate> load();
}

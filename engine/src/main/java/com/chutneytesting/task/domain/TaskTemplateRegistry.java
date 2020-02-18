package com.chutneytesting.task.domain;

import com.chutneytesting.task.spi.Task;
import java.util.Collection;
import java.util.Optional;

/**
 * Registry for {@link TaskTemplate}.
 */
public interface TaskTemplateRegistry {

    /**
     * Refresh all available {@link TaskTemplate} based on given {@link TaskTemplateLoader}.<br>
     * Main use case, except for initialization, is when {@link Task} classes are added to the classpath at runtime.
     */
    void refresh();

    /**
     * @return a {@link TaskTemplate} or empty if the given type did not matched any registered {@link TaskTemplate}
     */
    Optional<TaskTemplate> getByIdentifier(String identifier);

    Collection<TaskTemplate> getAll();
}

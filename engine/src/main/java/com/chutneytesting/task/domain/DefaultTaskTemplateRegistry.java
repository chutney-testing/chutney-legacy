package com.chutneytesting.task.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTaskTemplateRegistry implements TaskTemplateRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskTemplateRegistry.class);

    private final TaskTemplateLoaders loaders;
    private final Map<String, TaskTemplate> taskTemplatesByType = new ConcurrentHashMap<>();

    public DefaultTaskTemplateRegistry(TaskTemplateLoaders taskTemplateLoader) {
        this.loaders = taskTemplateLoader;
        refresh();
    }

    @Override
    public synchronized void refresh() {
        taskTemplatesByType.clear();
        loaders
            .orderedTemplates()
            .forEach(taskTemplate -> {
                TaskTemplate alreadyStoredTaskTemplate = taskTemplatesByType.putIfAbsent(taskTemplate.identifier(), taskTemplate);
                if (alreadyStoredTaskTemplate != null) {
                    LOGGER.warn("Unable to register TaskTemplate[" + taskTemplate.identifier() + " (" + taskTemplate.implementationClass().getName() + ")]: already defined by " + alreadyStoredTaskTemplate.implementationClass().getName());
                } else {
                    LOGGER.debug("Task registered: " + taskTemplate.identifier() + " (" + taskTemplate.implementationClass().getName() + ")");
                }
            });
    }

    @Override
    public Optional<TaskTemplate> getByIdentifier(String identifier) {
        return Optional.ofNullable(taskTemplatesByType.get(identifier));
    }

    @Override
    public Collection<TaskTemplate> getAll() {
        return taskTemplatesByType.values();
    }
}

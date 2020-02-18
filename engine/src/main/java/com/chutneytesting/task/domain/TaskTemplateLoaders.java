package com.chutneytesting.task.domain;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A Collection of loaders, allow to get all {@link TaskTemplate} in order, respecting each {@link TaskTemplateLoader} precedence.
 */
public class TaskTemplateLoaders {

    private final List<TaskTemplateLoader> loaders;

    public TaskTemplateLoaders(List<TaskTemplateLoader> loaders) {
        this.loaders = loaders;
    }

    List<TaskTemplate> orderedTemplates() {
        return loaders
            .stream()
            .flatMap(delegate -> delegate.load().stream())
            .collect(Collectors.toList());
    }
}

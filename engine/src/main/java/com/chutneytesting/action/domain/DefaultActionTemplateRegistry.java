package com.chutneytesting.action.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActionTemplateRegistry implements ActionTemplateRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionTemplateRegistry.class);

    private final ActionTemplateLoaders loaders;
    private final Map<String, ActionTemplate> actionTemplatesByType = new ConcurrentHashMap<>();

    public DefaultActionTemplateRegistry(ActionTemplateLoaders actionTemplateLoader) {
        this.loaders = actionTemplateLoader;
        refresh();
    }

    @Override
    public synchronized void refresh() {
        actionTemplatesByType.clear();
        loaders
            .orderedTemplates()
            .forEach(actionTemplate -> {
                ActionTemplate alreadyStoredActionTemplate = actionTemplatesByType.putIfAbsent(actionTemplate.identifier(), actionTemplate);
                if (alreadyStoredActionTemplate != null) {
                    LOGGER.warn("Unable to register ActionTemplate[" + actionTemplate.identifier() + " (" + actionTemplate.implementationClass().getName() + ")]: already defined by " + alreadyStoredActionTemplate.implementationClass().getName());
                } else {
                    LOGGER.debug("Action registered: " + actionTemplate.identifier() + " (" + actionTemplate.implementationClass().getName() + ")");
                }
            });
    }

    @Override
    public Optional<ActionTemplate> getByIdentifier(String identifier) {
        return Optional.ofNullable(actionTemplatesByType.get(identifier));
    }

    @Override
    public Collection<ActionTemplate> getAll() {
        return actionTemplatesByType.values();
    }
}

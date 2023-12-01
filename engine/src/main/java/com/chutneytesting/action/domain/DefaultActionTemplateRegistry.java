/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

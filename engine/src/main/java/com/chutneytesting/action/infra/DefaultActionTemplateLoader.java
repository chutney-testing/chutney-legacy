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

package com.chutneytesting.action.infra;

import com.chutneytesting.action.domain.ParsingError;
import com.chutneytesting.action.domain.ResultOrError;
import com.chutneytesting.action.domain.ActionTemplate;
import com.chutneytesting.action.domain.ActionTemplateLoader;
import com.chutneytesting.action.domain.ActionTemplateParser;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the Action type to load as {@link ActionTemplate} using an appropriate {@link ActionTemplateParser}
 */
public class DefaultActionTemplateLoader<T> implements ActionTemplateLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionTemplateLoader.class);

    private final String extensionFileName;
    private final Class<T> actionInterface;
    private final ActionTemplateParser<T> actionTemplateParser;

    public DefaultActionTemplateLoader(String extensionFileName, Class<T> actionInterface, ActionTemplateParser<T> actionTemplateParser) {
        this.extensionFileName = extensionFileName;
        this.actionInterface = actionInterface;
        this.actionTemplateParser = actionTemplateParser;
    }

    @Override
    public List<ActionTemplate> load() {
        return loadClasses()
            .map(actionTemplateParser::parse)
            .peek(this::warnIfParsingError)
            .filter(ResultOrError::isOk)
            .map(parsingResult -> parsingResult.result())
            .collect(Collectors.toList());
    }

    private Stream<Class<? extends T>> loadClasses() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/" + extensionFileName)
            .load()
            .stream()
            .peek(this::warnIfNotAction)
            .filter(this::isAction)
            .map(clazz -> (Class<? extends T>) clazz);
    }

    private void warnIfNotAction(Class<?> clazz) {
        if (!isAction(clazz)) {
            LOGGER.warn("Unable to load " + clazz.getName() + ": not a " + actionInterface.getName());
        }
    }

    private boolean isAction(Class<?> clazz) {
        return actionInterface.isAssignableFrom(clazz);
    }

    private void warnIfParsingError(ResultOrError<ActionTemplate, ParsingError> parsingResult) {
        if (parsingResult.isError()) {
            LOGGER.warn("Unable to parse Action[" + parsingResult.error().actionClass().getName() + "]: " + parsingResult.error().errorMessage());
        }
    }
}

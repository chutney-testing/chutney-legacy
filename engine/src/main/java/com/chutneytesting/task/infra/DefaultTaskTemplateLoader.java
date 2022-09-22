package com.chutneytesting.task.infra;

import com.chutneytesting.task.domain.ParsingError;
import com.chutneytesting.task.domain.ResultOrError;
import com.chutneytesting.task.domain.TaskTemplate;
import com.chutneytesting.task.domain.TaskTemplateLoader;
import com.chutneytesting.task.domain.TaskTemplateParser;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the Task type to load as {@link TaskTemplate} using an appropriate {@link TaskTemplateParser}
 */
public class DefaultTaskTemplateLoader<T> implements TaskTemplateLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskTemplateLoader.class);

    private final String extensionFileName;
    private final Class<T> taskInterface;
    private final TaskTemplateParser<T> taskTemplateParser;

    public DefaultTaskTemplateLoader(String extensionFileName, Class<T> taskInterface, TaskTemplateParser<T> taskTemplateParser) {
        this.extensionFileName = extensionFileName;
        this.taskInterface = taskInterface;
        this.taskTemplateParser = taskTemplateParser;
    }

    @Override
    public List<TaskTemplate> load() {
        return loadClasses()
            .map(taskTemplateParser::parse)
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
            .peek(this::warnIfNotTask)
            .filter(this::isTask)
            .map(clazz -> (Class<? extends T>) clazz);
    }

    private void warnIfNotTask(Class<?> clazz) {
        if (!isTask(clazz)) {
            LOGGER.warn("Unable to load " + clazz.getName() + ": not a " + taskInterface.getName());
        }
    }

    private boolean isTask(Class<?> clazz) {
        return taskInterface.isAssignableFrom(clazz);
    }

    private void warnIfParsingError(ResultOrError<TaskTemplate, ParsingError> parsingResult) {
        if (parsingResult.isError()) {
            LOGGER.warn("Unable to parse Task[" + parsingResult.error().taskClass().getName() + "]: " + parsingResult.error().errorMessage());
        }
    }
}

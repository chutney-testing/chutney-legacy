package com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.tools.Streams.identity;
import static java.util.Optional.empty;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.task.domain.TaskTemplateRegistry;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlacioAdapterSpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlacioAdapterSpringConfiguration.class);

    @Autowired
    TaskTemplateRegistry taskTemplateRegistry;

    @Bean
    public TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.glacio.parsers")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(this::instantiateGlacioParser))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(identity(c -> LOGGER.info("Loading glacio parser : " + c.getClass().getSimpleName() + " with priority " + c.priority())))
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(GlacioExecutableStepParser::priority))));
    }

    private Optional<GlacioExecutableStepParser> instantiateGlacioParser(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if (!GlacioExecutableStepParser.class.isAssignableFrom(clazz)) {
            LOGGER.warn(clazz.getSimpleName() + " is declared as glacio parser but does not implement GlacioExecutableStepParser class. Ignore it.");
            return empty();
        }

        Class<GlacioExecutableStepParser> parserClazz = (Class<GlacioExecutableStepParser>) clazz;
        try {
            return Optional.of(parserClazz.getConstructor(TaskTemplateRegistry.class).newInstance(taskTemplateRegistry));
        } catch (ReflectiveOperationException e) {
            return Optional.of(parserClazz.newInstance());
        }
    }
}

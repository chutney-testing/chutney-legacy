package com.chutneytesting.engine.api.glacio;

import static com.chutneytesting.tools.Streams.identity;

import com.chutneytesting.engine.api.glacio.parse.GlacioExecutableStepParser;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlacioAdapterSpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlacioAdapterSpringConfiguration.class);

    @Bean
    TreeSet<GlacioExecutableStepParser> glacioExecutableStepParsers() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.glacio.parsers")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(GlacioAdapterSpringConfiguration::<GlacioExecutableStepParser>instantiate))
            .map(identity(c -> LOGGER.debug("Loading glacio parser : " + c.getClass().getSimpleName() + " with priority " + c.priority())))
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(GlacioExecutableStepParser::priority))));
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return (T) clazz.newInstance();
    }
}

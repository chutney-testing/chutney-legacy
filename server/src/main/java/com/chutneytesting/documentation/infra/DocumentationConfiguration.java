package com.chutneytesting.documentation.infra;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Fat jar loading: @see https://github.com/spring-projects/spring-boot/issues/7161
 */
@Configuration
public class DocumentationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationConfiguration.class);
    private static final String EXAMPLES_FOLDER = "doc/examples";

    @Bean
    @Qualifier("embeddedExamples")
    Map<String, String> embeddedExamples() {

        Map<String, String> examples = Collections.emptyMap();
        URL resource = DocumentationConfiguration.class.getClassLoader().getResource(EXAMPLES_FOLDER);

        if (resource != null) {
            try {

                String scheme = resource.toURI().getScheme();

                if ("jar".equals(scheme)) {
                    examples = getFromFatJar();
                }
                else if ("file".equals(scheme)) {
                    examples = getFromFileSystem();
                }
                else {
                    LOGGER.warn("Cannot load embedded examples: Unknown scheme " + scheme );
                }

            } catch (URISyntaxException e) {
                LOGGER.warn("Cannot load embedded examples", e);
            }
        }
        else {
            LOGGER.warn("Cannot load embedded examples: Resource " + EXAMPLES_FOLDER + " is null");
        }

        return examples;
    }

    private Map<String, String> getFromFileSystem() {
        Map<String, String> examples = Collections.emptyMap();

        return examples;
    }

    private Map<String, String> getFromFatJar() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(DocumentationConfiguration.class.getClassLoader());

        try {
            List<URI> uris = Arrays.stream(resolver.getResources("classpath*:" + EXAMPLES_FOLDER + "/*"))
                .map(DocumentationConfiguration::getURI)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

            FileSystems.newFileSystem(uris.get(0), Collections.emptyMap());
            return uris.stream().collect(Collectors.toMap(
                uri -> Paths.get(uri).getFileName().toString(),
                uri -> {
                    try {
                        return CharStreams.toString(new InputStreamReader(uri.toURL().openStream()));
                    } catch (IOException e) {
                        return "{}";
                    }
                }
            ));

        } catch (IOException e) {
            LOGGER.warn("Cannot load embedded examples");
        }

        return Collections.emptyMap();
    }

    private static Optional<URI> getURI(Resource r) {
        try {
            return Optional.of(r.getURI());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}

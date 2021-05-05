package com.chutneytesting.task.function;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.SpelFunction;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ClasspathFunctions {

    @SpelFunction
    public static String resourcePath(String name) throws URISyntaxException {
        return resourceToPath(name).toString();
    }

    @SpelFunction
    public static List<String> resourcesPath(String name) throws IOException, URISyntaxException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(name);
        List<String> paths = new ArrayList<>();
        while (urls.hasMoreElements()) {
            paths.add(
                uriToPath(urls.nextElement().toURI()).toString()
            );
        }
        return paths;
    }

    @SpelFunction
    public static String resourceContent(String name, String charset) throws URISyntaxException, IOException {
        return Files.readString(resourceToPath(name), ofNullable(charset).map(Charset::forName).orElse(Charset.defaultCharset()));
    }

    private static Path resourceToPath(String name) throws URISyntaxException {
        URI uri = Objects.requireNonNull(
            Thread.currentThread().getContextClassLoader().getResource(name)
        ).toURI();

        return uriToPath(uri);
    }

    private static Path uriToPath(URI uri) {
        return Paths.get(uri);
    }
}

package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ClasspathFunctions {

    @SpelFunction
    public static String resourcePath(String name) throws URISyntaxException {
        URI uri = Objects.requireNonNull(
            Thread.currentThread().getContextClassLoader().getResource(name)
        ).toURI();

        return uriToPath(uri);
    }

    @SpelFunction
    public static List<String> resourcesPath(String name) throws IOException, URISyntaxException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(name);
        List<String> paths = new ArrayList<>();
        while (urls.hasMoreElements()) {
            paths.add(
                uriToPath(urls.nextElement().toURI())
            );
        }
        return paths;
    }

    private static String uriToPath(URI uri) {
        return Paths.get(uri).toString();
    }
}

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

package com.chutneytesting.action.function;

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.SpelFunction;
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
        var paths = new ArrayList<String>();
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

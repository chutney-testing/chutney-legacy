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

package com.chutneytesting.tools.loader;

import static com.chutneytesting.tools.loader.ExtensionLoaders.Mappers.instantiate;
import static com.chutneytesting.tools.loader.ExtensionLoaders.Mappers.splitByLine;
import static com.chutneytesting.tools.loader.ExtensionLoaders.Sources.classpath;

import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoader.ExtensionLoaderSource;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reference common {@link ExtensionLoader ExtensionLoaders}.
 */
public abstract class ExtensionLoaders {
    private ExtensionLoaders() {
    }

    /**
     * @param path to config files to read
     * @return and extension loader behaving like {@link java.util.ServiceLoader} except it loads classes instead of instances
     */
    public static ExtensionLoader<Class<?>> classpathToClass(String path) {
        return ExtensionLoader.Builder
            .<String, Class<?>>withSource(classpath(path))
            .withMapper(splitByLine().andThen(instantiate()));
    }

    /**
     * Reference common {@link ExtensionLoaderSource ExtensionLoaderSources}.
     */
    public static abstract class Sources {
        private Sources() {
        }

        /**
         * @param path to the files to load from in classpath
         * @return an {@link ExtensionLoaderSource}
         */
        public static ExtensionLoaderSource<String> classpath(String path) {
            return () -> {
                try {
                    Set<String> sources = new HashSet<>();
                    for (URL url : Collections.list(ExtensionLoaders.class.getClassLoader().getResources(path))) {
                        try (InputStream is = url.openStream()) {
                            sources.add(CharStreams.toString(new InputStreamReader(is)));
                        }
                    }
                    return sources;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }
    }

    /**
     * Reference common {@link Function Functions} usable as mapper for {@link ExtensionLoader}.
     */
    public static abstract class Mappers {
        private Mappers() {
        }

        /**
         * @return a function splitting {@link String} by line, ignoring comments starting with '#' character
         */
        public static Function<String, Set<String>> splitByLine() {
            return splitByLineIgnoring("#");
        }

        /**
         * @return a function splitting {@link String} by line, ignoring comments starting with the given character
         */
        public static Function<String, Set<String>> splitByLineIgnoring(String commentMarker) {
            return document -> Arrays
                .stream(document.split("\\r?\\n"))
                .filter(line -> !line.trim().startsWith(commentMarker) && !line.trim().isEmpty())
                .collect(Collectors.toSet());
        }

        /**
         * @return a function mapping a {@link Set} of {@link String} to a {@link Set} of {@link Class}
         */
        public static Function<Set<String>, Set<Class<?>>> instantiate() {
            return classnames -> classnames.stream().map(ThrowingFunction.toUnchecked(Class::forName)).collect(Collectors.toSet());
        }
    }
}

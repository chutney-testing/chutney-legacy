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

package com.chutneytesting.glacio.util;

import static com.chutneytesting.tools.Streams.identity;
import static java.lang.String.format;
import static java.util.Optional.empty;

import com.chutneytesting.glacio.domain.parser.IParseExecutableStep;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserClasspathReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserClasspathReader.class);

    public static Map<Pair<Locale, String>, IParseExecutableStep> createGlacioParsers(String classpath) {
        List<IParseExecutableStep> glacioExecutableStepParsers = createExecutableStepParsers(classpath);
        Optional<Map<Pair<Locale, String>, IParseExecutableStep>> result = glacioExecutableStepParsers.stream()
            .map(ParserClasspathReader::parserPairKeywords)
            .reduce(ParserClasspathReader::mergeParserPairKeywords);
        return result.orElseGet(HashMap::new);
    }

    private static List<IParseExecutableStep> createExecutableStepParsers(String classpath) {
        return ExtensionLoaders
            .classpathToClass(classpath)
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(ParserClasspathReader::instantiateGlacioParser))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(identity(c -> LOGGER.debug("Loading glacio parser : {}", c.getClass().getSimpleName())))
            .collect(Collectors.toList());
    }

    private static Optional<IParseExecutableStep> instantiateGlacioParser(Class<?> clazz) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (!IParseExecutableStep.class.isAssignableFrom(clazz)) {
            LOGGER.warn("{} is declared as glacio parser but does not implement GlacioExecutableStepParser interface. Ignore it.", clazz.getSimpleName());
            return empty();
        }
        Class<IParseExecutableStep> parserClazz = (Class<IParseExecutableStep>) clazz;
        return Optional.of(parserClazz.getDeclaredConstructor().newInstance());
    }

    private static Map<Pair<Locale, String>, IParseExecutableStep> parserPairKeywords(IParseExecutableStep parser) {
        return parser.keywords().entrySet().stream()
            .flatMap(e -> e.getValue().stream().map(v -> Pair.of(e.getKey(), v)))
            .collect(Collectors.toMap(o -> o, o -> parser));
    }

    private static Map<Pair<Locale, String>, IParseExecutableStep> mergeParserPairKeywords(Map<Pair<Locale, String>, IParseExecutableStep> o1,
                                                                                    Map<Pair<Locale, String>, IParseExecutableStep> o2) {
        o2.forEach((k, v) -> o1.merge(k, v, (p1, p2) -> {
            throw new IllegalArgumentException(format("Same pair {} declared for parsers {} and {}. Take the first one.", k, p1.getClass().getSimpleName(), p2.getClass().getSimpleName()));
        }));
        return o1;
    }
}

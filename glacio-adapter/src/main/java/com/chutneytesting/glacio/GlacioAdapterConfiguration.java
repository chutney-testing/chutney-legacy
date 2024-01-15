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

package com.chutneytesting.glacio;

import static com.chutneytesting.glacio.util.GherkinLanguageFileReader.createLanguagesKeywords;
import static com.chutneytesting.glacio.util.ParserClasspathReader.createGlacioParsers;

import com.chutneytesting.ExecutionConfiguration;
import com.chutneytesting.environment.EnvironmentConfiguration;
import com.chutneytesting.glacio.api.GlacioAdapter;
import com.chutneytesting.glacio.domain.parser.IParseExecutableStep;
import com.chutneytesting.glacio.domain.parser.StepFactory;
import com.chutneytesting.glacio.domain.parser.StepFactory.EXECUTABLE_KEYWORD;
import com.chutneytesting.glacio.domain.parser.executable.DefaultGlacioParser;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class GlacioAdapterConfiguration {

    private final ExecutionConfiguration executionConfiguration;
    private final EnvironmentConfiguration environmentConfiguration;

    private final GlacioAdapter glacioAdapter;

    public GlacioAdapterConfiguration(String envFolderPath) throws IOException {
        this.executionConfiguration = new ExecutionConfiguration();
        this.environmentConfiguration = new EnvironmentConfiguration(envFolderPath);

        final StepFactory stepFactory = createExecutableStepFactory();
        this.glacioAdapter = new GlacioAdapter(stepFactory);
    }

    public GlacioAdapter glacioAdapter() {
        return glacioAdapter;
    }

    public ExecutionConfiguration executionConfiguration() {
        return executionConfiguration;
    }

    private StepFactory createExecutableStepFactory() throws IOException {
        Map<Locale, Map<EXECUTABLE_KEYWORD, Set<String>>> languagesKeywords = createLanguagesKeywords(EXECUTABLE_KEYWORD.class, "META-INF/extension/chutney.glacio-languages.json");
        Map<Pair<Locale, String>, IParseExecutableStep> glacioParsers = createGlacioParsers("META-INF/extension/chutney.glacio.parsers");
        DefaultGlacioParser defaultGlacioParser = new DefaultGlacioParser(executionConfiguration.actionTemplateRegistry(), environmentConfiguration.getEmbeddedTargetApi());
        return new StepFactory(
            languagesKeywords,
            glacioParsers,
            defaultGlacioParser
        );
    }

}

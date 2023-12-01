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


import static com.chutneytesting.glacio.util.GherkinLanguageFileReader.createLanguagesKeywords;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.glacio.domain.parser.StepFactory;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GherkinLanguageFileReaderTest {

    private final Locale testLang = new Locale("tt", "TT");

    @Test
    public void should_support_languages_keyword_for_executable_steps() throws IOException {
        assertSupportLanguagesKeywordForExecutableSteps(Locale.ENGLISH, "Do", "Run");
        assertSupportLanguagesKeywordForExecutableSteps(Locale.FRENCH, "Fait", "Exécute", "Execute");
    }

    @Test
    public void should_support_languages_keyword_for_executable_steps_extensions() throws IOException {
        assertSupportLanguagesKeywordForExecutableSteps(Locale.ENGLISH, "ENTEST");
        assertSupportLanguagesKeywordForExecutableSteps(testLang, "TEST", "TST");
    }

    private void assertSupportLanguagesKeywordForExecutableSteps(Locale lang, String... keywords) throws IOException {
        Map<Locale, Map<StepFactory.EXECUTABLE_KEYWORD, Set<String>>> languagesKeywords = createLanguagesKeywords(StepFactory.EXECUTABLE_KEYWORD.class, "META-INF/extension/chutney.glacio-languages.json");
        assertThat(languagesKeywords).containsKeys(lang);
        Map<StepFactory.EXECUTABLE_KEYWORD, Set<String>> fr = languagesKeywords.get(lang);
        assertThat(fr).containsKeys(StepFactory.EXECUTABLE_KEYWORD.DO);
        assertThat(fr.get(StepFactory.EXECUTABLE_KEYWORD.DO)).contains(keywords);
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.LocaleUtils;

public final class GherkinLanguageFileReader {

    public static <T extends Enum<T>> Map<Locale, Map<T, Set<String>>> createLanguagesKeywords(Class<T> enumType, String path) throws IOException {
        Enumeration<URL> urls = GherkinLanguageFileReader.class.getClassLoader().getResources(path);
        Map<Locale, Map<T, Set<String>>> executableStepLanguagesKeywords = new HashMap<>();
        for (URL url : Collections.list(urls)) {
            try (InputStream is = url.openStream()) {
                JSONObject langObject = (JSONObject) JSONValue.parse(is);
                langObject.forEach((lang, keyWordObject) -> {
                    Locale localeKey = LocaleUtils.toLocale(lang);
                    Map<T, Set<String>> keywords =
                        Optional.ofNullable(executableStepLanguagesKeywords.get(localeKey)).orElseGet(HashMap::new);
                    ((JSONObject) keyWordObject).forEach((k, keyword) -> {
                        T execKeyword = Enum.valueOf(enumType, k.toUpperCase());
                        Set<String> keywordsSet = Optional.ofNullable(keywords.get(execKeyword)).orElseGet(HashSet::new);
                        keywordsSet.addAll((List<String>) keyword);
                        keywords.put(execKeyword, keywordsSet);
                    });
                    executableStepLanguagesKeywords.put(localeKey, keywords);
                });
            }
        }
        return executableStepLanguagesKeywords;
    }

}

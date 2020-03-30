package com.chutneytesting.engine.api.glacio;

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

    public static <T extends Enum<T>> Map<Locale, Map<T, Set<String>>> readAsMapLocale(Class<T> enumType, Enumeration<URL> urls) throws IOException {
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

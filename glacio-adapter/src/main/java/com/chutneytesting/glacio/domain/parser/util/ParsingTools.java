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

package com.chutneytesting.glacio.domain.parser.util;

import com.github.fridujo.glacio.model.Step;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingTools {

    private ParsingTools() {}

    public static Step removeKeyword(Pattern pattern, Step step) {
        final Matcher matcher = pattern.matcher(step.getText());
        if (matcher.matches()) {
            final String keyword = matcher.group("keyword");
            return new Step(step.isBackground(), step.getKeyword(), step.getText().substring(keyword.length()).trim(), step.getArgument(), step.getSubsteps());
        }
        throw new IllegalStateException();
    }

    public static String arrayToOrPattern(String... startingWords) {
        return Arrays.stream(startingWords).reduce((s, s2) -> s + "|" + s2).orElse("");
    }
}

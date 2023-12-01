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

import com.chutneytesting.action.spi.SpelFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFunction {

    @Deprecated
    @SpelFunction
    public static String str_replace(String input, String regularExpression, String replacement) {
        return stringReplace(input, regularExpression, replacement);
    }

    @SpelFunction
    public static String stringReplace(String input, String regularExpression, String replacement) {
        Matcher m = Pattern.compile(regularExpression).matcher(input);
        StringBuilder text = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(text, replacement);
        }
        m.appendTail(text);
        return text.toString();
    }
}

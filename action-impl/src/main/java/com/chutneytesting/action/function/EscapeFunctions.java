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
import org.apache.commons.text.StringEscapeUtils;

public class EscapeFunctions {

    @SpelFunction
    public static String escapeJson(String text) {
        return StringEscapeUtils.escapeJson(text);
    }

    @SpelFunction
    public static String unescapeJson(String text) {
        return StringEscapeUtils.unescapeJson(text);
    }

    @SpelFunction
    public static String escapeXml10(String text) {
        return StringEscapeUtils.escapeXml10(text);
    }

    @SpelFunction
    public static String escapeXml11(String text) {
        return StringEscapeUtils.escapeXml11(text);
    }

    @SpelFunction
    public static String unescapeXml(String text) {
        return StringEscapeUtils.unescapeXml(text);
    }

    @SpelFunction
    public static String escapeHtml3(String text) {
        return StringEscapeUtils.escapeHtml3(text);
    }

    @SpelFunction
    public static String unescapeHtml3(String text) {
        return StringEscapeUtils.unescapeHtml3(text);
    }

    @SpelFunction
    public static String escapeHtml4(String text) {
        return StringEscapeUtils.escapeHtml4(text);
    }

    @SpelFunction
    public static String unescapeHtml4(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }

    @SpelFunction
    public static String escapeSql(String sql) {
        return sql.replaceAll("'", "''");
    }
}

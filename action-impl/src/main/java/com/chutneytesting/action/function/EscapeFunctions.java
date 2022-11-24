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

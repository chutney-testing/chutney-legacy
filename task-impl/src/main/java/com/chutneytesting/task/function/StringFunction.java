package com.chutneytesting.task.function;

import com.chutneytesting.task.spi.SpelFunction;
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

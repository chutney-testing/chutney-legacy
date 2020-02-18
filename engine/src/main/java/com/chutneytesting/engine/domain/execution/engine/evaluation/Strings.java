package com.chutneytesting.engine.domain.execution.engine.evaluation;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Strings {
    private Strings() {
    }

    public static String replaceExpressions(String template, Function<String, Object> evaluator, String prefix, String suffix) {
        final StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(escapeForRegex(prefix) + "(.*?)" + escapeForRegex(suffix), Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String key = matcher.group(1);
            Object o = evaluator.apply(key);
            if (o != null) {
                String replacement = Matcher.quoteReplacement(String.valueOf(o));
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Object replaceExpression(String template, Function<String, Object> transformer, String prefix, String suffix) {
        final Pattern pattern = Pattern.compile("^" + escapeForRegex(prefix) + "(.*?)" + escapeForRegex(suffix) + "$", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(template);
        if (matcher.matches()) {
            String key = matcher.group(1);
            return transformer.apply(key);
        } else {
            return template;
        }
    }

    public static String escapeForRegex(String literal) {
        return literal
            .replaceAll("\\$", Matcher.quoteReplacement("\\$"))
            .replaceAll("\\{", Matcher.quoteReplacement("\\{"))
            .replaceAll("\\}", Matcher.quoteReplacement("\\}"));
    }
}

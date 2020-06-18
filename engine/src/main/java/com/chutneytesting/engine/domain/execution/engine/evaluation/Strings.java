package com.chutneytesting.engine.domain.execution.engine.evaluation;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class Strings {

    private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();

    private Strings() {
    }

    public static String replaceExpressions(String template, Function<String, Object> evaluator, String prefix, String suffix, String escape) {
        final StringBuffer sb = new StringBuffer();
        Pattern pattern = cachePattern("(" + escapeForRegex(escape) + ")?" + escapeForRegex(prefix) + "(.*?)" + escapeForRegex(suffix));
        final Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String escapeMatch = matcher.group(1);
            String key = matcher.group(2);
            if (escapeMatch == null) {
                Object o = evaluator.apply(key);
                if (o != null) {
                    String replacement = Matcher.quoteReplacement(String.valueOf(o));
                    matcher.appendReplacement(sb, replacement);
                }
            } else {
                String replacement = Matcher.quoteReplacement(prefix + key + suffix);
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Object replaceExpression(String template, Function<String, Object> transformer, String prefix, String suffix, String escape) {
        final Pattern pattern = cachePattern("^(" + escapeForRegex(escape) + ")?" + escapeForRegex(prefix) + "(.*?)" + escapeForRegex(suffix) + "$");
        final Matcher matcher = pattern.matcher(template.trim());
        if (matcher.matches()) {
            String escapeMatch = matcher.group(1);
            String key = matcher.group(2);
            if (escapeMatch == null) {
                return transformer.apply(key);
            } else {
                return prefix + key + suffix;
            }
        } else {
            return template;
        }
    }

    public static String escapeForRegex(String literal) {
        return literal
            .replaceAll("\\\\", Matcher.quoteReplacement("\\\\"))
            .replaceAll("\\$", Matcher.quoteReplacement("\\$"))
            .replaceAll("\\{", Matcher.quoteReplacement("\\{"))
            .replaceAll("\\}", Matcher.quoteReplacement("\\}"));
    }

    private static Pattern cachePattern(String pattern) {
        return ofNullable(PATTERN_CACHE.get(pattern)).orElseGet(() -> {
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.DOTALL);
            PATTERN_CACHE.put(pattern, compiledPattern);
            return compiledPattern;
        });
    }
}

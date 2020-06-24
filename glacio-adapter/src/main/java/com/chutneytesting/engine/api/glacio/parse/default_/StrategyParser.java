package com.chutneytesting.engine.api.glacio.parse.default_;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.glacio.parse.IParseStrategy;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import com.github.fridujo.glacio.model.Step;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StrategyParser implements IParseStrategy {

    private final static String STEP_STRATEGY_PATTERN_STRING = "(?<strategy>\\((?<name>[a-zA-Z_-]+):{1}(?<parameters>[^\\(]*)\\))";

    List<StrategyParser> parsers = Collections.emptyList();

    public StrategyParser() {}

    public StrategyParser(List<StrategyParser> parsers) {
        this.parsers = parsers;
    }

    public Map<Locale, Set<String>> keywords() {
        return Collections.emptyMap();
    }

    @Override
    public List<StepStrategyDefinition> parseStep(Locale lang, Step step) {
        return findStrategyGroups(step.getText())
            .entrySet().stream()
            .collect(Collectors.toMap(kv -> getStrategyParser(lang, kv.getKey()), Map.Entry::getValue))
            .entrySet().stream()
            .map( kv -> kv.getKey().toStrategyDef(lang, kv.getValue()))
            .collect(Collectors.toList());
    }

    public Map<String, String> findStrategyGroups(String text) {
        Map<String, String> strategies = new HashMap<>();
        Pattern p = Pattern.compile(STEP_STRATEGY_PATTERN_STRING);
        Matcher matcher = p.matcher(text);
        while(matcher.find()) {
            String name = ofNullable(matcher.group("name")).orElse("");
            String parameters = ofNullable(matcher.group("parameters")).orElse("");

            strategies.put(name, parameters);
        }

        return Collections.unmodifiableMap(strategies);
    }

    public StrategyParser getStrategyParser(Locale lang, String name) {
        return parsers.stream()
            .filter( p -> p.keywords().get(lang).contains(name))
            .findFirst()
            .orElse(this);
    }

    public StepStrategyDefinition toStrategyDef(Locale lang, String parameters) {
        return new StepStrategyDefinition("", parseProperties(lang, parameters));
    }

    public StrategyProperties parseProperties(Locale lang, String parameters) {
        return new StrategyProperties();
    }

}

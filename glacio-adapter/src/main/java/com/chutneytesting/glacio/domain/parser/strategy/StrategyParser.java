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

package com.chutneytesting.glacio.domain.parser.strategy;

import static java.util.Optional.ofNullable;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
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
import org.apache.commons.lang3.tuple.Pair;

public class StrategyParser implements IParseStrategy {

    private final String STEP_STRATEGY_PATTERN_STRING = "(?<strategy>\\((?<name>[a-zA-Z_-]+):{1}(?<parameters>[^\\(]*)\\))";
    private final Pattern pattern = Pattern.compile(STEP_STRATEGY_PATTERN_STRING, Pattern.DOTALL);

    List<StrategyParser> parsers = Collections.emptyList();

    public StrategyParser() {
    }

    public StrategyParser(List<StrategyParser> parsers) {
        this.parsers = parsers;
    }

    public Map<Locale, Set<String>> keywords() {
        return Collections.emptyMap();
    }

    @Override
    public List<StepDefinitionDto.StepStrategyDefinitionDto> parseGlacioStep(Locale lang, Step step) {
        return findStrategyGroups(step.getText())
            .entrySet().stream()
            .collect(Collectors.toMap(kv -> getStrategyParser(lang, kv.getKey()), Map.Entry::getValue))
            .entrySet().stream()
            .map(kv -> kv.getKey().toStrategyDef(lang, kv.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public Pair<Step, List<StepDefinitionDto.StepStrategyDefinitionDto>> parseStepAndStripStrategy(Locale lang, Step step) {
        return Pair.of(stripStrategyFrom(step), parseGlacioStep(lang, step));
    }

    public Step stripStrategyFrom(Step step) {
        String sentenceWithoutStrategy = stripStrategyFrom(step.getText());
        return new Step(step.isBackground(), step.getKeyword(), sentenceWithoutStrategy, step.getArgument(), step.getSubsteps());
    }

    private String stripStrategyFrom(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString().trim().replaceAll(" +", " ");
    }

    public Map<String, String> findStrategyGroups(String text) {
        Map<String, String> strategies = new HashMap<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = ofNullable(matcher.group("name")).orElse("");
            String parameters = ofNullable(matcher.group("parameters")).orElse("");

            strategies.put(name, parameters);
        }

        return Collections.unmodifiableMap(strategies);
    }

    public StrategyParser getStrategyParser(Locale lang, String name) {
        return parsers.stream()
            .filter(p -> p.keywords().get(lang).contains(name))
            .findFirst()
            .orElse(this);
    }

    public StepDefinitionDto.StepStrategyDefinitionDto toStrategyDef(Locale lang, String parameters) {
        return new StepDefinitionDto.StepStrategyDefinitionDto("", parseProperties(lang, parameters));
    }

    public StepDefinitionDto.StrategyPropertiesDto parseProperties(Locale lang, String parameters) {
        return new StepDefinitionDto.StrategyPropertiesDto();
    }
}

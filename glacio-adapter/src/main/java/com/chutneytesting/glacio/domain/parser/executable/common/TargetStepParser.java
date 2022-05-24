package com.chutneytesting.glacio.domain.parser.executable.common;

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.util.ParsingTools;
import com.github.fridujo.glacio.model.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TargetStepParser implements GlacioStepParser<TargetExecutionDto> {

    private final EnvironmentApi environmentApplication;

    private final Pattern startWithPredicate;
    private final Predicate<String> predicate;

    public TargetStepParser(EnvironmentApi environmentApplication, String... startingWords) {
        this.environmentApplication = environmentApplication;
        this.startWithPredicate = Pattern.compile("^(?<keyword>" + ParsingTools.arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.predicate = startWithPredicate.asPredicate();
    }

    @Override
    public TargetExecutionDto parseGlacioStep(ParsingContext context, Step step) {
        String environment = context.values.get(ENVIRONMENT);
        if (isNullOrEmpty(environment)) {
            throw new IllegalArgumentException("Cannot parse target if no environment provided");
        }
        return step.getSubsteps().stream()
            .filter(substep -> predicate.test(substep.getText()))
            .map(s -> ParsingTools.removeKeyword(startWithPredicate, s))
            .map(s -> parseTargetStep(environment, s))
            .findFirst()
            .orElse(null);
    }

    private TargetExecutionDto parseTargetStep(String environmentName, Step step) {
        return toTarget(environmentApplication.getTarget(environmentName, step.getText().trim()));
    }

    private TargetExecutionDto toTarget(TargetDto targetForExecution) {
        return new TargetExecutionDto(
            targetForExecution.name,
            targetForExecution.url,
            targetForExecution.properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value)),
            null // no agents
        );
    }
}

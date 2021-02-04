package com.chutneytesting.glacio.domain.parser.executable.common;

import static com.chutneytesting.glacio.domain.parser.ParsingContext.PARSING_CONTEXT_KEYS.ENVIRONMENT;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.chutneytesting.engine.domain.environment.SecurityInfoImpl;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.glacio.domain.parser.GlacioStepParser;
import com.chutneytesting.glacio.domain.parser.ParsingContext;
import com.chutneytesting.glacio.domain.parser.util.ParsingTools;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TargetStepParser implements GlacioStepParser<Target> {

    private final EmbeddedEnvironmentApi environmentApplication;

    private final Pattern startWithPredicate;
    private final Predicate<String> predicate;

    public TargetStepParser(EmbeddedEnvironmentApi environmentApplication, String... startingWords) {
        this.environmentApplication = environmentApplication;
        this.startWithPredicate = Pattern.compile("^(?<keyword>" + ParsingTools.arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.predicate = startWithPredicate.asPredicate();
    }

    @Override
    public Target parseGlacioStep(ParsingContext context, Step step) {
        String environment = context.values.get(ENVIRONMENT);
        if (isNullOrEmpty(environment)) {
            throw new IllegalArgumentException("Cannot parse target if no environment provided");
        }
        return step.getSubsteps().stream()
            .filter(substep -> predicate.test(substep.getText()))
            .map(s -> ParsingTools.removeKeyword(startWithPredicate, s))
            .map(s -> parseTargetStep(environment, s))
            .findFirst()
            .orElse(TargetImpl.NONE);
    }

    private Target parseTargetStep(String environmentName, Step step) {
        return toTarget(environmentApplication.getTarget(environmentName, step.getText().trim()));
    }

    private Target toTarget(TargetDto targetForExecution) {
        return TargetImpl.builder()
            .withName(targetForExecution.name)
            .withUrl(targetForExecution.url)
            .withProperties(targetForExecution.properties.stream().collect(Collectors.toMap(p -> p.key, p -> p.value)))
            .withSecurity(toSecurityInfo(targetForExecution))
            .build();
    }

    private SecurityInfoImpl toSecurityInfo(TargetDto targetDto) {
        return SecurityInfoImpl.builder()
            .credential(toCredential(targetDto))
            .keyStore(targetDto.keyStore)
            .keyStorePassword(targetDto.keyStorePassword)
            .privateKey(targetDto.privateKey)
            .build();
    }

    private SecurityInfoImpl.Credential toCredential(TargetDto targetDto) {
        if (isNullOrEmpty(targetDto.username) && isNullOrEmpty(targetDto.password)) {
            return SecurityInfoImpl.Credential.NONE;
        }
        return SecurityInfoImpl.Credential.of(targetDto.username, targetDto.password);
    }


}

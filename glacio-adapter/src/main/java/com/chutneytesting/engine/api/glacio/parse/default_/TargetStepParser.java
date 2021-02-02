package com.chutneytesting.engine.api.glacio.parse.default_;

import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.arrayToOrPattern;
import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.removeKeyword;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.SecurityInfoImpl;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.environment.api.EnvironmentEmbeddedApplication;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TargetStepParser implements StepParser<Target> {

    private final EnvironmentEmbeddedApplication environmentApplication;

    private final Pattern startWithPredicate;
    private final Predicate<String> predicate;

    public TargetStepParser(EnvironmentEmbeddedApplication environmentApplication, String... startingWords) {
        this.environmentApplication = environmentApplication;
        this.startWithPredicate = Pattern.compile("^(?<keyword>" + arrayToOrPattern(startingWords) + ")(?: .*)$");
        this.predicate = startWithPredicate.asPredicate();
    }

    @Override
    public Target parseStepForEnv(String env, Step step) {
        return step.getSubsteps().stream()
            .filter(substep -> predicate.test(substep.getText()))
            .map(s -> removeKeyword(startWithPredicate, s))
            .map(s -> parseTargetStep(env, s))
            .findFirst()
            .orElse(TargetImpl.NONE);
    }

    @Override
    public Target parseStep(Step step) {
        return parseStepForEnv("", step);
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
        if (isNullOrEmpty(targetDto.username ) && isNullOrEmpty(targetDto.password)) {
            return SecurityInfoImpl.Credential.NONE;
        }
        return SecurityInfoImpl.Credential.of(targetDto.username, targetDto.password);
    }

}

package com.chutneytesting.engine.api.glacio.parse.default_;

import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.arrayToOrPattern;
import static com.chutneytesting.engine.api.glacio.parse.default_.ParsingTools.removeKeyword;

import com.chutneytesting.design.domain.environment.EnvironmentService;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.engine.api.glacio.parse.StepParser;
import com.chutneytesting.engine.domain.environment.SecurityInfoImpl;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.task.spi.injectable.Target;
import com.github.fridujo.glacio.model.Step;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class TargetStepParser implements StepParser<Target> {

    private final EnvironmentService environmentService;

    private final Pattern startWithPredicate;
    private final Predicate<String> predicate;

    public TargetStepParser(EnvironmentService environmentService, String... startingWords) {
        this.environmentService = environmentService;
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
        return toTarget(environmentService.getTargetForExecution(environmentName, step.getText().trim()));
    }

    private Target toTarget(com.chutneytesting.design.domain.environment.Target targetForExecution) {
        return TargetImpl.builder()
            .withName(targetForExecution.name)
            .withUrl(targetForExecution.url)
            .withProperties(targetForExecution.properties)
            .withSecurity(toSecurityInfo(targetForExecution.security))
            .withAgents(targetForExecution.agents)
            .build();
    }

    private SecurityInfoImpl toSecurityInfo(SecurityInfo securityInfo) {
        return SecurityInfoImpl.builder()
            .credential(toCredential(securityInfo.credential))
            .keyStore(securityInfo.keyStore)
            .keyStorePassword(securityInfo.keyStorePassword)
            .trustStore(securityInfo.trustStore)
            .trustStorePassword(securityInfo.trustStorePassword)
            .privateKey(securityInfo.privateKey)
            .build();
    }

    private SecurityInfoImpl.Credential toCredential(SecurityInfo.Credential credential) {
        if (credential == null || SecurityInfo.Credential.NONE.equals(credential)) {
            return SecurityInfoImpl.Credential.NONE;
        }
        return SecurityInfoImpl.Credential.of(credential.username, credential.password);
    }

}

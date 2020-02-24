package com.chutneytesting.engine.domain.execution.engine;

import static com.chutneytesting.engine.domain.environment.NoTarget.NO_TARGET;
import static com.chutneytesting.engine.domain.environment.SecurityInfo.Credential;

import java.util.Optional;

import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.task.spi.FinallyAction;

class FinallyActionMapper {

    StepDefinition toStepDefinition(FinallyAction finallyAction) {
        return new StepDefinition(
            "Finally action generated",
            finallyAction.target()
                .map(this::mapTarget)
                .orElse(ImmutableTarget.copyOf(NO_TARGET)),
            finallyAction.actionIdentifier(),
            null,
            finallyAction.inputs(),
            null,
            null
        );
    }

    private Target mapTarget(com.chutneytesting.task.spi.injectable.Target target) {
        return ImmutableTarget.builder()
            .id(Target.TargetId.of(target.name()))
            .url(target.url())
            .security(mapSecu(target.security()))
            .properties(target.properties())
            .build();
    }

    private SecurityInfo mapSecu(com.chutneytesting.task.spi.injectable.SecurityInfo security) {
        return SecurityInfo.builder()
            .credential(mapCreds(security.credential()))
            .trustStore(security.trustStore().orElse(null))
            .trustStorePassword(security.trustStorePassword().orElse(null))
            .keyStore(security.keyStore().orElse(null))
            .keyStorePassword(security.keyStorePassword().orElse(null))
            .privateKey(security.privateKey().orElse(null))
            .build();
    }

    private Credential mapCreds(Optional<com.chutneytesting.task.spi.injectable.SecurityInfo.Credential> credential) {
        return credential.map(c -> Credential.of(c.username(), c.password()))
            .orElse(null);
    }
}

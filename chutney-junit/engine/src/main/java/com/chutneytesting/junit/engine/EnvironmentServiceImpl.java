package com.chutneytesting.junit.engine;

import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.junit.api.EnvironmentService;

public class EnvironmentServiceImpl implements EnvironmentService {

    private final EmbeddedEnvironmentApi delegate;

    public EnvironmentServiceImpl(EmbeddedEnvironmentApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addEnvironment(EnvironmentDto environment) {
        delegate.createEnvironment(environment);
    }

    @Override
    public void deleteEnvironment(String environmentName) {
        delegate.deleteEnvironment(environmentName);
    }

    @Override
    public void addTarget(String environmentName, TargetDto target) {
        delegate.addTarget(environmentName, target);
    }
}

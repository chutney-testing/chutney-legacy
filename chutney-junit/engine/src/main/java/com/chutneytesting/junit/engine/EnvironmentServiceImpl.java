package com.chutneytesting.junit.engine;

import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.junit.api.EnvironmentService;

public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentApi delegate;

    public EnvironmentServiceImpl(EnvironmentApi delegate) {
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
    public void addTarget(TargetDto target) {
        delegate.addTarget(target);
    }
}

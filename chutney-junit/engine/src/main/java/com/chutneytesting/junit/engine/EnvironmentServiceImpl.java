package com.chutneytesting.junit.engine;


import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.Target;

public class EnvironmentServiceImpl implements EnvironmentService {

    private final com.chutneytesting.environment.domain.EnvironmentService delegate;

    public EnvironmentServiceImpl(com.chutneytesting.environment.domain.EnvironmentService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addEnvironment(Environment environment) {
        delegate.createEnvironment(environment);
    }

    @Override
    public void deleteEnvironment(String environmentName) {
        delegate.deleteEnvironment(environmentName);
    }

    @Override
    public void addTarget(String environmentName, Target target) {
        delegate.addTarget(environmentName, target);
    }
}

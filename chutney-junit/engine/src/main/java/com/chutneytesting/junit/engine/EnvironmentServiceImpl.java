package com.chutneytesting.junit.engine;


import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.Target;

public class EnvironmentServiceImpl implements EnvironmentService {

    private com.chutneytesting.design.domain.environment.EnvironmentService delegate;

    public EnvironmentServiceImpl(com.chutneytesting.design.domain.environment.EnvironmentService delegate) {
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

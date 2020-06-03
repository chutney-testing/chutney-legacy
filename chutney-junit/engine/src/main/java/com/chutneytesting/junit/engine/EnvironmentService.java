package com.chutneytesting.junit.engine;

import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.Target;

public interface EnvironmentService {

    void addEnvironment(Environment environment);

    void deleteEnvironment(String environmentName);

    void addTarget(String environmentName, Target target);
}

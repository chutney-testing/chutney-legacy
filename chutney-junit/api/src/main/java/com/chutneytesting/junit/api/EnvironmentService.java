package com.chutneytesting.junit.api;

import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;

public interface EnvironmentService {

    void addEnvironment(EnvironmentDto environment);

    void deleteEnvironment(String environmentName);

    void addTarget(TargetDto target);
}

package com.chutneytesting.agent.domain.configure;

public interface Explorations {
    boolean changeStateToIfPossible(NetworkConfiguration networkConfiguration, ConfigurationState exploring);

    void remove(NetworkConfiguration networkConfiguration);
}

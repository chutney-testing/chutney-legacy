package com.chutneytesting.agent.domain.configure;

import static com.chutneytesting.agent.domain.configure.ConfigurationState.EXPLORING;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.FINISHED;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.NOT_STARTED;
import static com.chutneytesting.agent.domain.configure.ConfigurationState.WRAPING_UP;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ConfigurationStateTest {
    @Test
    public void configuration_state_transition_is_coherent() {
        assertThat(NOT_STARTED.canChangeTo(EXPLORING)).isTrue();
        assertThat(EXPLORING.canChangeTo(WRAPING_UP)).isTrue();
        assertThat(WRAPING_UP.canChangeTo(FINISHED)).isTrue();

        assertThat(NOT_STARTED.canChangeTo(WRAPING_UP)).isFalse();
        assertThat(EXPLORING.canChangeTo(EXPLORING)).isFalse();
    }
}

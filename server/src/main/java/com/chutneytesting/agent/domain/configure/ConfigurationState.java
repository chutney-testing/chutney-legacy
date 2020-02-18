package com.chutneytesting.agent.domain.configure;

public enum ConfigurationState {
    NOT_STARTED, EXPLORING, WRAPING_UP, FINISHED;

    public boolean canChangeTo(ConfigurationState state) {
        return state.ordinal() == ordinal() + 1;
    }
}

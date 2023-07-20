package com.chutneytesting.junit.engine;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.platform.engine.ConfigurationParameters;

public class SystemEnvConfigurationParameters implements ConfigurationParameters {

    private final ConfigurationParameters delegate;
    private final Map<String, String> env = System.getenv();

    public SystemEnvConfigurationParameters(ConfigurationParameters delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<String> get(String key) {
        Optional<String> delegateValue = delegate.get(key);

        if (delegateValue.isEmpty()) {
            return ofNullable(env.get(key));
        } else {
            return delegateValue;
        }
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        Optional<Boolean> delegateValue = delegate.getBoolean(key);

        if (delegateValue.isEmpty()) {
            return ofNullable(env.get(key)).map(Boolean::valueOf);
        } else {
            return delegateValue;
        }
    }

    @Override
    public int size() {
        return delegate.size() + env.size();
    }

    @Override
    public Set<String> keySet() {
        return env.keySet();
    }
}

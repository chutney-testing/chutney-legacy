package com.chutneytesting;

import static java.util.Optional.ofNullable;

import com.chutneytesting.action.spi.injectable.ActionsConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineActionsConfiguration implements ActionsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineActionsConfiguration.class);
    private final Map<String, String> configuration = new HashMap<>();

    public EngineActionsConfiguration(Map<String, String> configuration) {
        this.configuration.putAll(configuration);
    }

    @Override
    public String getString(String key) {
        return configuration.get(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return ofNullable(getString(key)).orElse(defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        String value = configuration.get(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException exception) {
                LOGGER.error("Cannot parse [{}] to Integer", value);
            }
        }
        return null;
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return ofNullable(getInteger(key)).orElse(defaultValue);
    }
}

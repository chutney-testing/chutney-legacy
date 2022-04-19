package com.chutneytesting.scenario.api.compose.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

enum ComposableStrategyType {

    DEFAULT("Default", ""),
    RETRY("Retry", "retry-with-timeout"),
    SOFT("Soft", "soft-assert");

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposableStrategyType.class);
    public final String name;
    public final String engineType;

    ComposableStrategyType(String name, String engineType) {
        this.name = name;
        this.engineType = engineType;
    }

    public static ComposableStrategyType fromName(String name) {
        for (ComposableStrategyType e : ComposableStrategyType.values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        LOGGER.warn("Unknown strategy [{}] mapped to default", name);
        return DEFAULT;
    }

    public static ComposableStrategyType fromEngineType(String engineType) {
        for (ComposableStrategyType e : ComposableStrategyType.values()) {
            if (e.engineType.equals(engineType)) {
                return e;
            }
        }
        LOGGER.warn("Unknown strategy [{}] mapped to default", engineType);
        return DEFAULT;
    }
}

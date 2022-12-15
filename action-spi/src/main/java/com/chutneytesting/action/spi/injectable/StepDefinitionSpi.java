package com.chutneytesting.action.spi.injectable;

import java.util.Map;

public interface StepDefinitionSpi {
    /**
     * Return step definition inputs.
     */
    Map<String, Object> inputs();
}

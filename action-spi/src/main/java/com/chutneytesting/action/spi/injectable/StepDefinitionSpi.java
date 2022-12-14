package com.chutneytesting.action.spi.injectable;

import java.util.Map;

public interface StepDefinitionSpi {
    /**
     * Data used by a matched extension, may be empty.
     */
    Map<String, Object> inputs();
}

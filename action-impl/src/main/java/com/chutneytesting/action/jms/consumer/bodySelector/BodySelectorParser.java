package com.chutneytesting.action.jms.consumer.bodySelector;

import java.util.Optional;

public interface BodySelectorParser {

    String description();

    /**
     * @throws IllegalArgumentException if the selector matches the parser but {@link BodySelector} cannot be built nonetheless
     */
    Optional<BodySelector> tryParse(String selector) throws IllegalArgumentException;
}

package com.chutneytesting.action.jms.consumer.bodySelector;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class BodySelectorFactory {

    private static final BodySelectorParser[] FACTORIES = new BodySelectorParser[]{
        new XpathBodySelectorParser()
    };

    /**
     * @throws IllegalArgumentException if the selector matches a parser but {@link BodySelector} cannot be built nonetheless
     */
    public BodySelector build(String selector) throws IllegalArgumentException {
        return Arrays
            .stream(FACTORIES)
            .map(factory -> factory.tryParse(selector))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Wrong bodySelector syntax. " + syntaxDescription()));
    }

    public String syntaxDescription() {
        return "Available syntaxes are:\n\t- " +
            Arrays
                .stream(FACTORIES)
                .map(BodySelectorParser::description)
                .collect(Collectors.joining("\n\t- "));

    }
}

package com.chutneytesting.action.jms.consumer.bodySelector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class BodySelectorFactoryTest {

    private final BodySelectorFactory bodySelectorFactory = new BodySelectorFactory();

    @Test
    public void can_build_selector_with_valid_syntax() {
        BodySelector bodySelector = bodySelectorFactory.build("XPATH 'boolean(/test)'");
        assertThat(bodySelector).isNotNull();
    }

    @Test
    public void building_selector_with_invalid_syntax_throws() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> bodySelectorFactory.build("test"))
            .withMessage("Wrong bodySelector syntax. Available syntaxes are:\n" +
                "\t- XPath selector: ^XPATH '(?<xpath>.+)'$");
    }
}

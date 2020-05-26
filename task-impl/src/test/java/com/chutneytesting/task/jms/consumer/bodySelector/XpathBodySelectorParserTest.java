package com.chutneytesting.task.jms.consumer.bodySelector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;
import org.junit.jupiter.api.Test;

public class XpathBodySelectorParserTest {

    private final BodySelectorParser parser = new XpathBodySelectorParser();

    @Test
    public void can_build_valid_body_selector() {
        Optional<BodySelector> optionalBodySelector = parser.tryParse("XPATH 'boolean(/lol)'");

        assertThat(optionalBodySelector).isPresent();
    }

    @Test
    public void invalid_body_selector_syntax_returns_empty() {
        Optional<BodySelector> optionalBodySelector = parser.tryParse("test");

        assertThat(optionalBodySelector).isEmpty();
    }

    @Test
    public void valid_selector_syntax_but_invalid_xpath_throws() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> parser.tryParse("XPATH '@'"))
            .withMessageContaining("Unable to compile '@'");
    }

    @Test
    public void xpath_body_selector_discards_map_message() {
        BodySelector bodySelector = getBodySelector();

        MapMessage mapMessage = mock(MapMessage.class);
        assertThat(bodySelector.match(mapMessage)).isFalse();
    }

    @Test
    public void xpath_body_selector_discards_unreadable_text_message() throws JMSException {
        BodySelector bodySelector = getBodySelector();

        TextMessage mapMessage = mock(TextMessage.class);
        when(mapMessage.getText()).thenThrow(new JMSException("test"));
        assertThat(bodySelector.match(mapMessage)).isFalse();
    }

    @Test
    public void xpath_body_selector_discards_text_message_with_invalid_xml() throws JMSException {
        BodySelector bodySelector = getBodySelector();

        TextMessage mapMessage = mock(TextMessage.class);
        when(mapMessage.getText()).thenReturn("<test");
        assertThat(bodySelector.match(mapMessage)).isFalse();
    }

    @Test
    public void xpath_body_selector_discards_text_message_with_valid_xml_but_unmatched_xpath() throws JMSException {
        BodySelector bodySelector = getBodySelector();

        TextMessage mapMessage = mock(TextMessage.class);
        when(mapMessage.getText()).thenReturn("<test1/>");
        assertThat(bodySelector.match(mapMessage)).isFalse();
    }

    @Test
    public void xpath_body_selector_matches_text_message_when_xpath_matches() throws JMSException {
        BodySelector bodySelector = getBodySelector();

        TextMessage mapMessage = mock(TextMessage.class);
        when(mapMessage.getText()).thenReturn("<test/>");
        assertThat(bodySelector.match(mapMessage)).isTrue();
    }

    private BodySelector getBodySelector() {
        return parser.tryParse("XPATH 'boolean(/test)'").get();
    }

    @Test
    public void description_contains_pattern() {
        assertThat(parser.description()).isEqualTo("XPath selector: ^XPATH '(?<xpath>.+)'$");
    }
}

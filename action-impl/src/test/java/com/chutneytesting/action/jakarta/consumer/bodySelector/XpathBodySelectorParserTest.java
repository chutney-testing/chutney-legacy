/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.action.jakarta.consumer.bodySelector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.TextMessage;
import java.util.Optional;
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

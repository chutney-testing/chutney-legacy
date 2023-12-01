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

package com.chutneytesting.action.jms.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.action.jms.consumer.bodySelector.BodySelector;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import org.junit.jupiter.api.Test;

public class SelectedMessageConsumerTest {

    @Test
    public void browsing_stops_when_reaching_max_depth() throws JMSException {
        QueueBrowser browser = mock(QueueBrowser.class, RETURNS_DEEP_STUBS);
        when(browser.getEnumeration().hasMoreElements()).thenReturn(true); // infinite enumeration
        when(browser.getEnumeration().nextElement()).thenReturn(mock(Message.class));
        BodySelector bodySelector = m -> false; // never matches
        SelectedMessageConsumer selectedMessageConsumer = new SelectedMessageConsumer(browser, bodySelector, 3);

        Optional<Message> message = selectedMessageConsumer.getMessage();

        assertThat(message).isEmpty();
        verify(browser.getEnumeration(), times(3)).nextElement();
    }

    @Test
    public void browsing_stops_when_there_is_no_more_messages() throws JMSException {
        QueueBrowser browser = mock(QueueBrowser.class, RETURNS_DEEP_STUBS);
        when(browser.getEnumeration().hasMoreElements()).thenReturn(false); // empty enumeration
        BodySelector bodySelector = m -> true; // always matches
        SelectedMessageConsumer selectedMessageConsumer = new SelectedMessageConsumer(browser, bodySelector, 3);

        Optional<Message> message = selectedMessageConsumer.getMessage();

        assertThat(message).isEmpty();
        verify(browser.getEnumeration(), times(0)).nextElement();
    }

    @Test
    public void browsing_stops_when_matching() throws JMSException {
        QueueBrowser browser = mock(QueueBrowser.class, RETURNS_DEEP_STUBS);
        when(browser.getEnumeration().hasMoreElements()).thenReturn(true); // infinite enumeration
        when(browser.getEnumeration().nextElement()).thenReturn(mock(Message.class));
        BodySelector bodySelector = m -> true; // always matches
        SelectedMessageConsumer selectedMessageConsumer = new SelectedMessageConsumer(browser, bodySelector, 3);

        Optional<Message> message = selectedMessageConsumer.getMessage();

        assertThat(message).isPresent();
        verify(browser.getEnumeration(), times(1)).nextElement();
    }
}

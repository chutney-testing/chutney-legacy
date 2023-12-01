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

package com.chutneytesting.action.jakarta.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import org.junit.jupiter.api.Test;

public class SimpleMessageConsumerTest {

    @Test
    public void getMessage_returns_message_when_MessageConsumer_receives() throws JMSException {
        MessageConsumer messageConsumer = mock(MessageConsumer.class);
        when(messageConsumer.receive(eq(444L))).thenReturn(mock(Message.class));
        SimpleMessageConsumer simpleMessageConsumer = new SimpleMessageConsumer(messageConsumer, 444L);

        assertThat(simpleMessageConsumer.getMessage()).isPresent();
    }

    @Test
    public void getMessage_returns_empty_when_no_message_is_received() throws JMSException {
        MessageConsumer messageConsumer = mock(MessageConsumer.class);
        when(messageConsumer.receive(eq(123L))).thenReturn(null);
        SimpleMessageConsumer simpleMessageConsumer = new SimpleMessageConsumer(messageConsumer, 123L);

        assertThat(simpleMessageConsumer.getMessage()).isEmpty();
    }
}

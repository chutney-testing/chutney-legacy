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

package com.chutneytesting.action.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.chutneytesting.action.jms.JmsCleanQueueAction;
import com.chutneytesting.action.jms.JmsConnectionFactory;
import com.chutneytesting.action.jms.consumer.Consumer;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import javax.jms.Message;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

@SuppressWarnings("unchecked")
public class JmsCleanQueueActionTest {

    private JmsCleanQueueAction jmsCleanQueueAction;
    private Consumer mockConsumer;
    private Target target = mock(Target.class, RETURNS_DEEP_STUBS);
    private Logger logger = mock(Logger.class);

    @BeforeEach
    public void setUp() {
        mockConsumer = mock(Consumer.class);

        jmsCleanQueueAction = new JmsCleanQueueAction(target, logger, "", "");

        JmsConnectionFactory jmsConnectionFactory = mock(JmsConnectionFactory.class);
        CloseableResource<Consumer> messageConsumer = mock(CloseableResource.class);
        when(messageConsumer.getResource()).thenReturn(mockConsumer);
        when(jmsConnectionFactory.createConsumer(any(), any(), any())).thenReturn(messageConsumer);

        setField(jmsCleanQueueAction, "jmsConnectionFactory", jmsConnectionFactory);
    }

    @Test
    public void should_retrieve_all_available_messages_from_jms_queue() throws Exception {
        // Given the queue contains some messages
        final int nbAvailableMessages = new Random().nextInt(10);
        OngoingStubbing<Optional<Message>> stub = when(mockConsumer.getMessage());
        for (int i = 0; i < nbAvailableMessages; i++) {
            Message message = mock(Message.class, RETURNS_DEEP_STUBS);
            stub = stub.thenReturn(Optional.of(message));
            when(message.getPropertyNames().hasMoreElements()).thenReturn(false);
        }
        stub.thenReturn(Optional.empty());

        // When JMS clean task is executed
        jmsCleanQueueAction.execute();

        // Then all available messages are removed with 'receiveNoWait' method call
        verify(mockConsumer, times(nbAvailableMessages + 1)).getMessage();
    }
}

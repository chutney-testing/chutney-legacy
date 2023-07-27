package com.chutneytesting.action.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.chutneytesting.action.jms.consumer.Consumer;
import com.chutneytesting.action.spi.injectable.Logger;
import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.tools.CloseableResource;
import jakarta.jms.Message;
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

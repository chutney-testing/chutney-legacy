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
